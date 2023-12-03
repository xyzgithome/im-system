package com.lld.im.tcp.register;

import com.lld.im.common.constant.Constants;
import org.I0Itec.zkclient.ZkClient;

/**
 * @description:
 * @author: lld
 * @version: 1.0
 */
public class ZKit {

    private ZkClient zkClient;

    public ZKit(ZkClient zkClient) {
        this.zkClient = zkClient;
    }

    /**
     * 创建父节点
     * im-coreRoot/tcp/ip:port
     */
    public void createRootNode(){
        // 判断root节点是否存在
        boolean exists = zkClient.exists(Constants.ImCoreZkRoot);
        if(!exists){
            // 不存在，创建临时节点
            zkClient.createPersistent(Constants.ImCoreZkRoot);
        }

        // 判断tcp节点是否存在
        boolean tcpExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootTcp);
        if(!tcpExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootTcp);
        }

        // 判断websocket是否存在
        boolean webExists = zkClient.exists(Constants.ImCoreZkRoot +
                Constants.ImCoreZkRootWeb);
        if(!webExists){
            zkClient.createPersistent(Constants.ImCoreZkRoot +
                    Constants.ImCoreZkRootWeb);
        }
    }

    /**
     * 判断子节点是否存在
     * ip+port
     *
     * @param path
     */
    public void createNode(String path){
        if(!zkClient.exists(path)){
            zkClient.createPersistent(path);
        }
    }
}
