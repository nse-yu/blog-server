package com.example.blogserver;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import com.example.exception.ApiRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class BlogController {
	private final JdbcService service;
	@Autowired
	public BlogController(JdbcService service) {
		this.service = service;
	}
	
	@CrossOrigin
	@GetMapping("/article/{id}")
	public PostArticle getArticle(@PathVariable String id) {
		PostArticle article = service.findById(id);
		
		//TODO:空インスタンスを処理したい
		if(article.equals(new PostArticle()))
			return new PostArticle();
		
		return article;
	}
	
	@CrossOrigin
	@GetMapping("/article/all")
	public List<PostArticle> getArticles(){
		return service.findAll();
	}
	
	@CrossOrigin
	@DeleteMapping("/article/{articleID}/delete")
	public ResponseEntity<Boolean> deleteArticle(@PathVariable String articleID){
		System.out.println("delete: "+articleID);
		boolean result = service.delete(articleID);
		return result ? 
				ResponseEntity.status(HttpStatus.OK).body(true)
				: 
				ResponseEntity.status(HttpStatus.NOT_MODIFIED).body(false);
	}
	
	@CrossOrigin
	@GetMapping("/article/search")
	public List<PostArticle> getArticleByRegex(@RequestParam("q") String target){
		List<PostArticle> articles;

		if((articles = service.findByRegex(target)).isEmpty()) {
			articles.add(new PostArticle());
			return articles;
		}
		return articles;
	}
	
	@CrossOrigin
	@GetMapping("/img/all")
	public List<String> getImgURLs(){
		return service.findAllOnlyImgURL();
	}
	
	@CrossOrigin
	@PostMapping(value="/img/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<Boolean> uploadImg(@RequestParam("img") MultipartFile file){
		boolean status = service.saveImg(file);
		return ResponseEntity.status(status ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(status);
	}
	
	@CrossOrigin
	@GetMapping("/tag/{tagID}")
	public List<PostArticle> getArticlesByTagId(@PathVariable Integer tagID){
		return service.findByTagId(tagID);
	}
	
	@CrossOrigin
	@PostMapping(value="/article/post",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<Boolean> postArticle(
			@RequestParam("img") MultipartFile file,
			@RequestParam("articleID") String articleID,
			@RequestParam("title") String title,
			@RequestParam("desc") String desc,
			@RequestParam("tagID") Integer tagID,
			@RequestParam("markdown") String markdown
			) {
	
		if(file == null)
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
		
		//==========情報の保存===========//
		//再編集の有無
		Boolean exists = service.existsById(articleID);
		String id = exists ? articleID : UUID.randomUUID().toString();
		//更新時刻の取得
		Calendar c = Calendar.getInstance();		
		Timestamp ts = new Timestamp(c.getTimeInMillis());
		//インスタンスへ格納
		PostArticle article = new PostArticle(id,title,desc,markdown,
				file.getResource().getFilename(),tagID,ts);
		
		return service.save(file,article,exists) ? 
				ResponseEntity.status(HttpStatus.OK).body(true) 
				:
				ResponseEntity.status(HttpStatus.NOT_FOUND).body(false);
	}
	
	@CrossOrigin
	@GetMapping("/img/{imgURL}")
	public byte[] sendImgResource(@PathVariable String imgURL) throws ApiRequestException {
		byte[] all;

		try(FileInputStream fis = new FileInputStream(Paths.get("src/main/resources/static/img/" + imgURL).toFile())) {
			all = fis.readAllBytes();
		} catch (IOException e) {
			throw new ApiRequestException(
					e.getClass().toString()+"has thrown because of your request's mistakes.",e);
		} finally {
			System.out.println("finally-block has executed.");
		}
		return all;
	}
}
