package com.lld.im.common.route.handler.consistenthash;

import com.lld.im.common.enums.UserErrorCode;
import com.lld.im.common.exception.ApplicationException;
import com.lld.im.common.route.handler.RouteHandler;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * hash一致性算法实现节点分配
 */
@Component(value = "routeHandler-3")
public class TreeMapConsistentHashRouteHandler extends AbstractConsistentHash implements RouteHandler {
    private TreeMap<Long, String> treeMap = new TreeMap<>();

    /**
     * 虚拟节点数
     */
    private static final int NODE_SIZE = 2;

    @Override
    protected void add(long key, String value) {
        // 加入虚拟节点
        for (int i = 0; i < NODE_SIZE; i++) {
            treeMap.put(super.hash("node" + key + i), value);
        }

        // 加入真实节点
        treeMap.put(key, value);
    }

    @Override
    protected String getFirstNodeValue(String value) {
        Long hash = super.hash(value);
        SortedMap<Long, String> last = treeMap.tailMap(hash);
        if (!last.isEmpty()) {
            return last.get(last.firstKey());
        }

        if (treeMap.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }

        return treeMap.firstEntry().getValue();
    }

    @Override
    protected void processBefore() {
        treeMap.clear();
    }

    @Override
    public String routeServer(List<String> nodeList, String userId) {
        if (nodeList.size() == 0) {
            throw new ApplicationException(UserErrorCode.SERVER_NOT_AVAILABLE);
        }
        return this.process(nodeList, userId);
    }
}
