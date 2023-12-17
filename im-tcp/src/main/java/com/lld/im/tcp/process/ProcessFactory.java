package com.lld.im.tcp.process;

public class ProcessFactory {
    private ProcessFactory() {
    }

    private static class ProcessHolder {
        private static final BaseProcess INSTANCE = new BaseProcess() {
            @Override
            public void processBefore() {

            }

            @Override
            public void processAfter() {

            }
        };
    }

    public static BaseProcess getMessageProcess() {
        return ProcessHolder.INSTANCE;
    }
}
