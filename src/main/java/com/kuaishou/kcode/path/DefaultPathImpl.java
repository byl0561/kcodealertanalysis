package com.kuaishou.kcode.path;

import com.kuaishou.kcode.alert.domain.RuleTypeEnum;
import com.kuaishou.kcode.common.ServicePairWithoutIP;
import com.kuaishou.kcode.common.StatisticalIndicators;
import com.kuaishou.kcode.utils.MathConverter;
import com.kuaishou.kcode.utils.ServicePairFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPathImpl implements Path {
    private Map<Integer, Map<ServicePairWithoutIP, StatisticalIndicators>> points = new ConcurrentHashMap<>();
    private Map<ServicePairWithoutIP, ServicePairWithoutIP> servicePairPool = new ConcurrentHashMap<>();
    private Graph graphP = new Graph(false);
    private Graph graphN = new Graph(true);
    private Map<ServicePairWithoutIP, Collection<List<String>>> pathCache = new HashMap<>();

    @Override
    public void addPoint(int time, ServicePairWithoutIP servicePair, StatisticalIndicators indicators) {
        Map<ServicePairWithoutIP, StatisticalIndicators> name2indicators = points.get(time);
        if (Objects.isNull(name2indicators)){
            points.putIfAbsent(time, new ConcurrentHashMap<>());
            name2indicators = points.get(time);
        }
        ServicePairWithoutIP cachedPair = servicePairPool.get(servicePair);
        if (Objects.isNull(cachedPair)){
            cachedPair = ServicePairFactory.clone(servicePair);
            if (Objects.isNull(servicePairPool.putIfAbsent(cachedPair, cachedPair))){
                graphP.addEdge(servicePair.getFromService(), servicePair.getToService());
                graphN.addEdge(servicePair.getToService(), servicePair.getFromService());
            }
            cachedPair = servicePairPool.get(servicePair);
        }
        name2indicators.put(cachedPair, indicators);
    }

    public static void main(String... args){
        StatisticalIndicators indicator = null;
        StringBuilder value = new StringBuilder();
        value.append(Objects.isNull(indicator) ? -1 : indicator.getP99()).append("ms,");
        if (Objects.isNull(indicator)){
            value.append("-1%");
        }
        else {
            MathConverter.addPercentageInStringBuilder(indicator.getSuccessRate(), value);
        }
        value.append(',');
        System.out.println(value);
    }

    @Override
    public Collection<String> getPath(ServicePairWithoutIP servicePair, int time, RuleTypeEnum ruleType) {
        if (!servicePairPool.containsKey(servicePair)){
            return Collections.emptyList();
        }
        Map<ServicePairWithoutIP, StatisticalIndicators> indicators = points.getOrDefault(time, Collections.emptyMap());
        Collection<String> collection = new ArrayList<>();
        Collection<List<String>> path = pathCache.getOrDefault(servicePair, new ArrayList<>());
        if (path.isEmpty()){
            Collection<List<String>> fromN = graphN.getLongestPaths(servicePair.getFromService());
            Collection<List<String>> toP = graphP.getLongestPaths(servicePair.getToService());
            fromN.forEach(fromPath -> {
                toP.forEach(toPath -> {
                    List<String> fp = new LinkedList<>(fromPath);
                    fp.addAll(toPath);
                    path.add(fp);
                });
            });
            pathCache.put(servicePair, path);
        }
        StringBuilder name = new StringBuilder();
        StringBuilder value = new StringBuilder();
        path.forEach(p -> {
            name.setLength(0);
            value.setLength(0);
            String[] lastService = new String[1];
            p.forEach(service -> {
                name.append(service).append("->");
                if (Objects.nonNull(lastService[0])){
                    StatisticalIndicators indicator = indicators.get(new ServicePairWithoutIP(lastService[0], service));
                    if (ruleType == RuleTypeEnum.P99){
                        value.append(Objects.isNull(indicator) ? -1 : indicator.getP99()).append("ms,");
                    }
                    else if (ruleType == RuleTypeEnum.SUCCESS_RATE){
                        if (Objects.isNull(indicator)){
                            value.append("-1%");
                        }
                        else {
                            MathConverter.addPercentageInStringBuilder(indicator.getSuccessRate(), value);
                        }
                        value.append(',');
                    }
                }
                lastService[0] = service;
            });
            name.setLength(name.length() - 2);
            name.append('|').append(value);
            name.setLength(name.length() - 1);
            collection.add(name.toString());
        });
        return collection;
    }

    private static class Graph{
        public Graph(boolean isReverse){
            this.isReverse = isReverse;
        }

        private Map<String, Point> pointMap = new ConcurrentHashMap<>();
        private Map<Point, Collection<List<String>>> pathCache = new ConcurrentHashMap<>();
        private boolean isReverse;

        public void addEdge (String from, String to){
            Point fromPoint = getPoint(from);
            Point toPoint = getPoint(to);
            toPoint.addFromPoint(fromPoint);
        }

        private Point getPoint(String name){
            Point point = pointMap.get(name);
            if (Objects.isNull(point)){
                pointMap.putIfAbsent(name, new Point(name));
                point = pointMap.get(name);
            }
            return point;
        }

        public Collection<List<String>> getLongestPaths(String name){
            return appendLongestPath(pointMap.get(name));
        }

        private Collection<List<String>> appendLongestPath(Point nowPoint){
            Collection<List<String>> paths = pathCache.getOrDefault(nowPoint, new ArrayList<>());
            if (!paths.isEmpty()){
                return paths;
            }
            if (nowPoint.nextPoints.isEmpty()){
                paths.add(Collections.singletonList(nowPoint.name));
            }
            else{
                nowPoint.nextPoints.forEach(point -> {
                    Collection<List<String>> nextPaths = appendLongestPath(point);
                    nextPaths.forEach(nextPath -> {
                        List<String> nl = new LinkedList<>();
                        if (isReverse){
                            nl.addAll(nextPath);
                            nl.add(nowPoint.name);
                        }
                        else {
                            nl.add(nowPoint.name);
                            nl.addAll(nextPath);
                        }
                        paths.add(nl);
                    });
                });
            }
            pathCache.put(nowPoint, paths);
            return paths;
        }


        private static class Point{

            public Point(String name){
                this.name = name;
            }

            private String name;
            private Set<Point> nextPoints = new HashSet<>();
            private int maxPath = 0;
            private Set<Point> fromPoints = new HashSet<>();

            public synchronized void addFromPoint(Point fromPoint){
                fromPoints.add(fromPoint);
                fromPoint.dealToPointChanges(this);
            }

            // WARN：不能成环
            public synchronized void dealToPointChanges(Point toPoint){
                if (toPoint.maxPath + 1 > this.maxPath){
                    nextPoints.clear();
                    nextPoints.add(toPoint);
                    this.maxPath = toPoint.maxPath + 1;
                    fromPoints.forEach(fromPoint -> {
                        fromPoint.dealToPointChanges(this);
                    });
                }
                else if (toPoint.maxPath + 1 == this.maxPath){
                    nextPoints.add(toPoint);
                }
            }

            @Override
            public boolean equals(Object o) {
                return name.equals(((Point) o).name);
            }

            @Override
            public int hashCode() {
                return name.hashCode();
            }
        }
    }
}
