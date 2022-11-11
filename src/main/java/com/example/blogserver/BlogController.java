package com.example.blogserver;

import com.example.docs.DocsAccessBuilder;
import com.example.docs.DocsAccessor;
import com.example.drive.DriveAccessBuilder;
import com.example.drive.DriveAccessor;
import com.example.exception.ApiRequestException;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.docs.v1.model.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

@Controller
public class BlogController {
	private final JdbcService service;
	private DocsAccessor 	docsAccessor;
	private DriveAccessor 	driveAccessor;
	private TokenResponse response;
	private String title;
	private String body;
	private String origin;


	@Autowired
	public BlogController(JdbcService service) {
		this.service = service;
	}

	
	@CrossOrigin
	@GetMapping("/article/{id}")
	@ResponseBody
	public PostArticle getArticle(@PathVariable String id) {
		PostArticle article = service.findById(id);
		
		//TODO:空インスタンスを処理したい
		if(article.equals(new PostArticle()))
			return new PostArticle();
		
		return article;
	}
	
	@CrossOrigin
	@GetMapping("/article/all")
	@ResponseBody
	public List<PostArticle> getArticles(){
		return service.findAll();
	}
	
	@CrossOrigin
	@ResponseBody
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
	@ResponseBody
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
	@ResponseBody
	@GetMapping("/img/all")
	public List<String> getImgURLs(){
		return service.findAllOnlyImgURL();
	}
	
	@CrossOrigin
	@ResponseBody
	@PostMapping(value="/img/upload",consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<Boolean> uploadImg(@RequestParam("img") MultipartFile file){
		boolean status = service.saveImg(file);
		return ResponseEntity.status(status ? HttpStatus.OK : HttpStatus.BAD_REQUEST).body(status);
	}
	
	@CrossOrigin
	@ResponseBody
	@GetMapping("/tag/{tagID}")
	public List<PostArticle> getArticlesByTagId(@PathVariable Integer tagID){
		return service.findByTagId(tagID);
	}
	
	@CrossOrigin
	@ResponseBody
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
	@ResponseBody
	@GetMapping("/img/{imgURL}")
	public byte[] sendImgResource(@PathVariable String imgURL) throws ApiRequestException {
		byte[] all;

		try(FileInputStream fis = new FileInputStream(Paths.get("src/main/resources/static/img/" + imgURL).toFile())) {
			all = fis.readAllBytes();
		} catch (IOException e) {
			throw new ApiRequestException(
					e.getClass().toString()+"has thrown because of your request's mistakes.",e);
		}

		return all;
	}

	@CrossOrigin
	@ResponseBody
	@PostMapping(value = "/docs/generate", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
	public ResponseEntity<String> generateDocument(
			@RequestParam("title") String title,
			@RequestParam("body") String body,
			@RequestParam("origin") String origin
	){

		// obtain form values
		this.title  = title;
		this.body   = body;
		this.origin = origin;

		//System.out.printf("Accepted values: [title: %s, body: %s]%n", title, body);

		// create Accessor for Google Docs API
		docsAccessor = DocsAccessBuilder
				.init("docs-web-app")
				.setCallback(System.out::println)
				.build();

		driveAccessor = DriveAccessBuilder
				.init("drive-web-app")
				.setCallback(System.out::println)
				.build();

		// sharing auth instances
		driveAccessor.setAuth(docsAccessor.getAuth());

		// authorization check before accessing API
		if(!docsAccessor.isAuthorized()){

			// trying to redirect browser to the Google auth page
			System.out.println(docsAccessor.redirectURI());
			return ResponseEntity.status(HttpStatus.SEE_OTHER).body(docsAccessor.redirectURI());

		}

		// create new documents and get new instances
		Document doc;
		try {

			doc = docsAccessor.create(title, body);

		} catch (IOException e) {

			if(((GoogleJsonResponseException) e).getStatusCode() == HttpStatus.UNAUTHORIZED.value())
				return ResponseEntity.status(HttpStatus.SEE_OTHER).body(docsAccessor.redirectURI());
			else
				throw new RuntimeException(e);

		}

		try {

			driveAccessor.moveFileToFolder(doc.getDocumentId(), "1FWUwm_6AeRu_LlPNGoarW8Sgwj8G0ai4");

		} catch (IOException e) {

			if(((GoogleJsonResponseException) e).getStatusCode() == HttpStatus.UNAUTHORIZED.value())
				return ResponseEntity.status(HttpStatus.SEE_OTHER).body(driveAccessor.redirectURI());
			else
				throw new RuntimeException(e);

		}

		return ResponseEntity.status(HttpStatus.CREATED).body(doc.getTitle());
	}

	@RequestMapping(value = "/docs/redirect", method = RequestMethod.GET)
	public String redirectedAndGenerate(
			@RequestParam("code") String code
	){

		// there is no response for this session
		if(response == null)
			response = docsAccessor.requestNewToken(code);

		// apply for the user authorization using Token response
		docsAccessor.authorize(response);
		response = null;

		// create new documents and get new instances
		Document doc = null;
		try {

			doc = docsAccessor.create(title, body);

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		try {

			driveAccessor.moveFileToFolder(doc.getDocumentId(), "1FWUwm_6AeRu_LlPNGoarW8Sgwj8G0ai4");

		} catch (IOException e) {

			throw new RuntimeException(e);

		}

		System.out.println("redirect to "+origin);

		return "redirect:"+origin;
	}
}
