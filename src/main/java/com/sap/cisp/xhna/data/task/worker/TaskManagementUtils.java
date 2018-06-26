package com.sap.cisp.xhna.data.task.worker;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Task management utility class
 *
 */
public class TaskManagementUtils {

    /*
     * Declaration of address book functions This declaration will be used by
     * both AddressBookClient and AddressBookWorkerAgent
     */
    public static enum FunctionEnum {
        FIND_SOCIAL_ACCOUNT_BY_KEYWORD(0), VERIFY_SOCIAL_ACCOUNT(1), CANCEL_SINGLE_TASK(
                2), CANCEL_MULTIPLE_TASK(3), ADD_TASK(4), GET_TOKEN_TAG(5), RELEASE_TOKEN_TAG(
                6), CHECK_IF_UNCACHED_SOCIAL_POSTS(7), CHECK_TRADITIONAL_POST_CACHED(
                8), ADD_TRADITIONAL_POST_TO_CACHE(9), GET_UNCACHED_SOCIAL_POSTS(
                10), REGISTER_WORKER_IDENTIFIER(11), DEREGISTER_WORKER_IDENTIFIER(
                12), UNSUBSCRIBE_STREAM_CSDL(13), REGISTER_CSDL_WITH_WORKER_IDENTIFIER(
                14), TEST_TASK(99);

        private final int index;

        private FunctionEnum(int index) {
            this.index = index;
        }

        @Override
        public String toString() {
            return Integer.toString(index);
        }

        public int getInt() {
            return index;
        }
    }

    public static FunctionEnum getEnumByIndex(int index) {
        switch (index) {
        case 0:
            return FunctionEnum.FIND_SOCIAL_ACCOUNT_BY_KEYWORD;
        case 1:
            return FunctionEnum.VERIFY_SOCIAL_ACCOUNT;
        case 2:
            return FunctionEnum.CANCEL_SINGLE_TASK;
        case 3:
            return FunctionEnum.CANCEL_MULTIPLE_TASK;
        case 4:
            return FunctionEnum.ADD_TASK;
        case 5:
            return FunctionEnum.GET_TOKEN_TAG;
        case 6:
            return FunctionEnum.RELEASE_TOKEN_TAG;
        case 7:
            return FunctionEnum.CHECK_IF_UNCACHED_SOCIAL_POSTS;
        case 8:
            return FunctionEnum.CHECK_TRADITIONAL_POST_CACHED;
        case 9:
            return FunctionEnum.ADD_TRADITIONAL_POST_TO_CACHE;
        case 10:
            return FunctionEnum.GET_UNCACHED_SOCIAL_POSTS;
        case 11:
            return FunctionEnum.REGISTER_WORKER_IDENTIFIER;
        case 12:
            return FunctionEnum.DEREGISTER_WORKER_IDENTIFIER;
        case 13:
            return FunctionEnum.UNSUBSCRIBE_STREAM_CSDL;
        case 14:
            return FunctionEnum.REGISTER_CSDL_WITH_WORKER_IDENTIFIER;
        default:
            return FunctionEnum.TEST_TASK;
        }
    }

    /**
     * Byte array to Object
     * 
     * @param bytes
     * @return
     */
    public static Object byteToObject(byte[] bytes) {
        Object obj = null;
        try {
            // byte array to object
            ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
            ObjectInputStream oi = new ObjectInputStream(bi);

            obj = oi.readObject();
            bi.close();
            oi.close();
        } catch (Exception e) {
            System.out.println("Translation " + e.getMessage());
            e.printStackTrace();
        }
        return obj;
    }

    /**
     * Object to byte array
     * 
     * @param obj
     * @return
     */
    public static byte[] objectToByte(java.lang.Object obj) throws Exception {
        byte[] bytes = null;
        try {
            // object to byte array
            ByteArrayOutputStream bo = new ByteArrayOutputStream(512);
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("Translation " + e.getMessage());
            throw e;
        }
        return bytes;
    }

    /**
     * Object array to byte array
     * 
     * @param obj
     * @return
     */
    public static byte[] objectsToByte(java.lang.Object[] obj) {
        byte[] bytes = null;
        try {
            // object to byte array
            ByteArrayOutputStream bo = new ByteArrayOutputStream();
            ObjectOutputStream oo = new ObjectOutputStream(bo);
            oo.writeObject(obj);

            bytes = bo.toByteArray();

            bo.close();
            oo.close();
        } catch (Exception e) {
            System.out.println("Translation " + e.getMessage());
            e.printStackTrace();
        }
        return bytes;
    }
}
