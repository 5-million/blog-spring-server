package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.exception.category.CategoryNotExistException;
import eol_g.blog.exception.post.PostNotExistException;
import eol_g.blog.repository.CategoryRepository;
import eol_g.blog.repository.PostRepository;
import eol_g.blog.service.AwsS3Service;
import eol_g.blog.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

public abstract class AbstractPostService {

    @Autowired protected FileService fileService;
    @Autowired protected AwsS3Service awsS3Service;
    @Autowired protected PostRepository postRepository;
    @Autowired protected CategoryRepository categoryRepository;

    public abstract List<PostListDTO> getAll();
    public abstract PostDetailDTO getById(Long id) throws IOException;

    /**
     * 카테고리별 공개된 포스트
     */
    public List<PostListDTO> getByCategory(String category) {
        // 공개된 포스트 엔티티 리스트
        List<Post> postList = getPostEntityListByCategory(category);

        return PostListDTO.toDTO(postList);
    }

    /**
     * 레포지토리에서 post entity를 가져옴
     * id에 해당하는 포스트가 없을 경우 PostNotExistException 예외 발생
     */
    protected Post getPostEntityById(Long id) {
        // 포스트 엔티티
        Optional<Post> optional = postRepository.findById(id);
        if (!optional.isPresent()) throw new PostNotExistException();

        return optional.get();
    }

    /**
     * 모든 post entity list를 가져오는 함수
     * 포스트가 존재하지 않는 경우 PostNotExistException 예외 발생
     */
    protected List<Post> getPostEntityList() {
        Optional<List<Post>> optional = postRepository.findAll();
        if (!optional.isPresent()) throw new PostNotExistException();

        return optional.get();
    }

    /**
     * 카테고리별 post entity list를 가져오는 함수
     * 검색 결과가 없을 경우 PostNotExistException 예외 발생
     */
    protected List<Post> getPostEntityListByCategory(String category) {
        Optional<List<Post>> optional = postRepository.findByCategory(category);
        if(!optional.isPresent()) throw new PostNotExistException();

        return optional.get();
    }

    /**
     * FileService를 이용해 포스트의 내용을 가져오는 함수
     */
    protected String getContent(Post post) throws IOException {
        String content;

        try {
            // 로컬 파일에서 포스트 내용을 가져옴
            content = fileService.getContent(post.getObjectKey());
        } catch (FileNotFoundException exception) {
            // 로컬에 포스트 파일이 존재하지 않을 경우 s3에서 포스트 내용을 가져옴
            content = awsS3Service.getObjectContent(post.getObjectKey());
            // 로컬에 포스트 파일 재생성
            fileService.createFile(post.getObjectKey(), content);
        }

        return content;
    }

    /**
     * 카테고리 이름으로 엔티티 가져오기
     */
    protected Category getCategoryEntityByName(String name) {
        Optional<Category> optional = categoryRepository.findByName(name);

        if(!optional.isPresent())
            throw new CategoryNotExistException();

        return optional.get();
    }
}
