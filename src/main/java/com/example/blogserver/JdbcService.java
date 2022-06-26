package com.example.blogserver;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class JdbcService {
	private static final String rel_path = "src/main/resources/static/img/";
	private final JdbcTemplate template;
	private final RowMapper<PostArticle> mapper = (resultSet,rowNum) -> {
		PostArticle article = new PostArticle();
		article.setArticleID(resultSet.getString("articleID"));
		article.setTitle(resultSet.getString("title"));
		article.setDesc(resultSet.getString("desc"));
		article.setContent(resultSet.getString("markdown"));
		article.setTagID(resultSet.getInt("tagID"));
		article.setLastUpdate(resultSet.getDate("lastupdate"));
		article.setImgURL(resultSet.getString("imgURL"));
		return article;
	};
	
	@Autowired
	public JdbcService(JdbcTemplate template) {
		this.template = template;
	}
	
	/*====================取得系====================*/
	/**IDによる記事１つの取得*/
	public PostArticle findById(String id) {
		PostArticle article;
		try {
			article = template.queryForObject("SELECT * FROM article WHERE articleID = '"+id+"'",mapper);
		} catch (DataAccessException e) {
			e.printStackTrace();
			return new PostArticle();
		}	
		return article;
	}
	
	/**全記事を取得*/
	public List<PostArticle> findAll(){
		List<PostArticle> articles = new ArrayList<>();
		try {
			articles = template.query("SELECT * FROM article", mapper);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return articles;
	}
	/**tagIDによる分類取得*/
	public List<PostArticle> findByTagId(Integer tagID){
		List<PostArticle> articles = new ArrayList<>();
		
		try {
			articles = template.query("SELECT * FROM article WHERE tagID = "+tagID, mapper);
		} catch (DataAccessException e) {
			e.printStackTrace();
		}
		return articles;
	}
	/**画像urlのみ取得*/
	public List<String> findAllOnlyImgURL(){
		List<String> imgs = new ArrayList<>();
		File img_file = new File(rel_path);
		try {
			for (File img : Objects.requireNonNull(img_file.listFiles()))
				imgs.add(img.getName());
		} catch(NullPointerException e){
			e.printStackTrace();
		}
		return imgs;
	}
	/**title,desc,markdownから一致するものがあった記事のみ取得*/
	public List<PostArticle> findByRegex(String target) {
		List<PostArticle> articles;
		String queryString = String.format(
				"SELECT * FROM article WHERE title LIKE '%%%s%%' OR desc LIKE '%%%s%%' OR markdown LIKE '%%%s%%'",target,target,target);
		try {
			articles = template.query(queryString, mapper);
		} catch (DataAccessException e) {
			e.printStackTrace();
			return null;
		}
		return articles;
	}
	
	/*=================保存系=================*/
	/**投稿された１つの記事を保存*/
	public boolean save(MultipartFile file,PostArticle article,Boolean exists) {
		String sqlString = !exists ? 
				"INSERT INTO article(articleID,title,desc,markdown,imgURL,tagID,lastupdate) VALUES(?,?,?,?,?,?,?)"
				:
				"UPDATE article SET title = ?,desc = ?,markdown = ?,imgURL = ?,tagID = ?,lastupdate = ? WHERE articleID = ?";

		/*false処理*/
		if(file.isEmpty())
			return false;
		if(article.equals(new PostArticle()))
			return false;
		
		/*==========true処理==========*/
		/*image fileコピー*/
		File img_file = null;
		try {
			img_file = new File(rel_path+file.getResource().getFilename());
			OutputStream os = new FileOutputStream(img_file);
			os.write(file.getBytes());
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		/*データ挿入*/
		try {
			if(exists) {
				template.update(sqlString,
						article.getTitle(),
						article.getDesc(),
						article.getContent(),
						img_file.getName(),
						article.getTagID(),
						article.getLastUpdate(),
						article.getArticleID()
				);
			}else {
				template.update(sqlString,
						article.getArticleID(),
						article.getTitle(),
						article.getDesc(),
						article.getContent(),
						img_file.getName(),
						article.getTagID(),
						article.getLastUpdate()
				);
			}
		} catch (DataAccessException e) {
			e.printStackTrace();
			return false;
		}
		/*全行程終了*/
		return true;
	}
	
	public boolean saveImg(MultipartFile file) {
		/*image fileコピー*/
		File img_file;
		try {
			img_file = new File(rel_path+file.getResource().getFilename());
			OutputStream os = new FileOutputStream(img_file);
			os.write(file.getBytes());
			os.flush();
			os.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/*=================確認系=================*/
	public boolean existsById(String id) {
		String sqlString = "SELECT * FROM article WHERE articleID = '"+id+"' LIMIT 1";
		return !template.query(sqlString, mapper).isEmpty();
	}
	
	/*=================削除系=================*/
	public boolean delete(String id) {
		String sqlString = "DELETE FROM article WHERE articleID = '"+id+"' LIMIT 1";
		return template.update(sqlString) >= 1;
	}
}
