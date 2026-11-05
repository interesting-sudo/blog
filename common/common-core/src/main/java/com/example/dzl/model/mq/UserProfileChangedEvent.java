package com.example.dzl.model.mq;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserProfileChangedEvent implements Serializable {

    private Long userId;

    private String nickname;

    private String avatar;
}

