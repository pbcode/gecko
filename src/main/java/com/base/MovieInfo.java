package com.base;

import lombok.Data;

import java.io.Serializable;

/**
 * Demo class
 *
 * @author pengbo3
 */
@Data
public class MovieInfo implements Serializable {

    private static final long serialVersionUID = -2313538272791927856L;
    private String movieName;
}
