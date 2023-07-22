package com.example.studentformdemo.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor
@Data
@ToString
public class StudentDTO implements Serializable {
    private String name;
    private String city;
    private String email;
    private int level;

}
