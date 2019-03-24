package com.base;

import lombok.Data;

import java.io.Serializable;


/**
 * Demo class
 *
 * @author pengbo3
 */
@Data
public class PbRpcRequest implements Serializable {

    private static final long serialVersionUID = 315479669764300843L;
    private String requestId;

    private String className;

    private String methodName;

    private Object[] params;
}
