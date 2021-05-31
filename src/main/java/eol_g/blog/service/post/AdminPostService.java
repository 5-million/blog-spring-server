package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.dto.PostUpdateDTO;
import eol_g.blog.dto.PostUploadDTO;
import eol_g.blog.exception.post.PostDuplicateException;
import eol_g.blog.exception.post.PostNotTempException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AdminPostService extends AbstractPostService {

    @Override
    public List<PostListDTO> getAll() {
        List<Post> postList = getPostEntityList();

        return PostListDTO.toDTO(postList);
    }

    /**
     * id로 포스트 정보 가져오기
     */
    @Override
    public PostDetailDTO getById(Long id) throws IOException {
        // 포스트 엔티티
        Post post = getPostEntityById(id);

        // 포스트 내용 가져오기
        String content = getContent(post);

        // PostDetailDTO 생성
        return PostDetailDTO.toDTO(post, content);
    }

    /**
     * 포스트 업로드
     */
    @Transactional
    public Long upload(PostUploadDTO postDto) throws IOException {
        PostStatus status = postDto.getStatus();
        String category = postDto.getCategory();
        String subject = postDto.getSubject();
        String content = postDto.getContent();

        // 포스트 중복 검사
        validateDuplicatePost(subject);

        // 카테고리 엔티티
        Category postCategory = getCategoryEntityByName(category);

        // 파일 생성
        String pathname = createPathname(status, category, subject);
        File file = fileService.createFile(pathname, content);

        // S3에 업로드
        String s3Key = awsS3Service.upload(pathname, file);

        // post db에 저장 후 post id 리턴
        Post post = Post.builder()
                .category(postCategory)
                .subject(subject)
                .filePath(file.getPath())
                .s3Key(s3Key)
                .status(status)
                .build();

        return postRepository.save(post);
    }

    /**
     * 포스트 수정
     */
    @Transactional
    public void update(Long id, PostUpdateDTO updateDto) throws IOException {
        // 기존 포스트 엔티티
        Post targetPost = getPostEntityById(id);

        // 새로운 카테고리 엔티티
        Category newCategory = getCategoryEntityByName(updateDto.getCategory());

        // 기존 포스트 파일
        File postFile = new File(targetPost.getFilePath());

        // 파일의 내용을 수정
        fileService.writeContent(updateDto.getContent(), postFile);

        // s3 객체 내용 수정
        awsS3Service.upload(targetPost.getS3Key(), postFile);

        // 포스트 파일을 수정된 제목과 카테고리에 맞게 이동
        String newPathname = createPathname(targetPost.getStatus(), updateDto.getCategory(), updateDto.getSubject());
        fileService.move(targetPost.getFilePath(), newPathname);

        // s3 객체의 제목과 카테고리를 수정
        awsS3Service.move(targetPost.getS3Key(), newPathname);

        // 엔티티 업데이트
        targetPost.update(newCategory, updateDto.getSubject(), newPathname, newPathname);
    }

    /**
     * 포스트 삭제
     */
    @Transactional
    public void delete(Long id) {
        // 포스트 엔티티
        Post targetPost = getPostEntityById(id);

        // File 포스트 삭제
        File postFile = new File(targetPost.getFilePath());
        postFile.delete();

        // s3에서 삭제
        awsS3Service.delete(targetPost.getS3Key());

        // DB 포스트 삭제
        postRepository.delete(targetPost);
    }

    /**
     * 임시저장 상태의 포스트를 공개 상태로 전환
     */
    @Transactional
    public void convertToPublic(Long id) {
        // 포스트 엔티티
        Post post = getPostEntityById(id);

        // 포스트가 임시상태인지 검사
        if(post.getStatus() != PostStatus.TEMP)
            throw new PostNotTempException();

        // 포스트 파일
        File postFile = new File(post.getFilePath());

        // 옮겨질 pathname
        String newPathname = createPathname(PostStatus.PUBLIC, post.getCategory().getName(), post.getSubject());

        // 포스트 temp → public 폴더 이동
        postFile.renameTo(new File(newPathname));

        // s3 포스트 temp → public으로 이동
        awsS3Service.move(post.getS3Key(), newPathname);

        // db filePath, s3Key, status update
        post.updateFilePath(newPathname);
        post.updateS3Key(newPathname);
        post.updateStatus(PostStatus.PUBLIC);
    }

    /**
     * 포스트 중복 검사
     */
    private void validateDuplicatePost(String subject) {
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new PostDuplicateException();
        });
    }

    /**
     * 저장될 파일의 pathname 생성
     */
    public String createPathname(PostStatus status, String category, String subject) {
        return "posts/" + status.toString().toLowerCase() + "/"
                + category + "/"
                + subject.replace(" ", "_")
                + ".md";
    }
}
