package com.dawn.common.auditlog;

public final class LogConstant {
    private LogConstant() {}

    public static class Action {
        public static final String CREATE = "CREATE";
        public static final String UPDATE = "UPDATE";
        public static final String DELETE = "DELETE";
        public static final String IMPORT_STOCK = "IMPORT_STOCK";
        public static final String TRANSITION_ORDER = "TRANSITION_ORDER";
        public static final String APPROVE = "APPROVE";
        public static final String REJECT = "REJECT";
    }

    public static class Entity {
        public static final String PRODUCT = "PRODUCT";
        public static final String VOUCHER = "VOUCHER";
        public static final String WAREHOUSE = "WAREHOUSE";
        public static final String SUB_ORDER = "SUB_ORDER";
        public static final String USER = "USER";
        public static final String ORDER = "ORDER";
        public static final String CATEGORY = "CATEGORY";
        public static final String RETURN_REQUEST = "RETURN_REQUEST";
    }

    public static class Status {
        public static final String SUCCESS = "SUCCESS";
        public static final String FAILED = "FAILED";
        public static final String DENIED = "DENIED";
    }
}
