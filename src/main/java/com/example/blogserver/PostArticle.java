package com.example.blogserver;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PostArticle {
	private String articleID;
	private String title;
	private String desc;
	private String content;
	private String imgURL;
	private Integer tagID;
	private Date lastUpdate;
}
