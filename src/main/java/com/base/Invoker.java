package com.base;

import lombok.Data;

@Data
public class Invoker {

    private String serviceName;
    private String url;
    private int weight;
}
