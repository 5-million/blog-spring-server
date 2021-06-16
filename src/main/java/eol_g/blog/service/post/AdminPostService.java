package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostObjectKey;
import eol_g.blog.domain.PostStatus;
import eol_g.blog.dto.PostDetailDTO;
import eol_g.blog.dto.PostListDTO;
import eol_g.blog.dto.PostUpdateDTO;
import eol_g.blog.dto.PostUploadDTO;
import eol_g.blog.exception.post.PostDuplicateException;
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

        // 포스트 오브젝트 키 생성
        String objectKey = PostObjectKey.builder()
                .category(postCategory)
                .subject(subject)
                .status(status)
                .build()
                .make();

        // 파일 생성
        File file = fileService.createFile(objectKey, content);

        // S3에 업로드
        String s3Key = awsS3Service.upload(objectKey, file);

        // post db에 저장 후 post id 리턴
        Post post = Post.builder()
                .category(postCategory)
                .subject(subject)
                .objectKey(objectKey)
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
        Post post = getPostEntityById(id);
        String oldObjectKey = post.getObjectKey();

        // 새로운 카테고리 엔티티
        Category newCategory = getCategoryEntityByName(updateDto.getCategory());

        // 기존 포스트 파일
        File postObject = new File(post.getObjectKey());

        // 파일의 내용을 수정
        fileService.writeContent(updateDto.getContent(), postObject);

        // s3 객체 내용 수정
        awsS3Service.upload(post.getObjectKey(), postObject);

        // 엔티티 업데이트
        post.update(newCategory, updateDto.getSubject());

        // 포스트 오브젝트 이동
        movePostObject(oldObjectKey, post.getObjectKey());
    }

    /**
     * 카테고리 또는 제목이 변경되어 ObjectKey가 변경된 경우 그에 맞게 object를 이동
     */
    private void movePostObject(String sourceObjectKey, String destinationObjectKey) {
        if (!sourceObjectKey.equals(destinationObjectKey)) {
            fileService.move(sourceObjectKey, destinationObjectKey);
            awsS3Service.move(sourceObjectKey, destinationObjectKey);
        }
    }

    /**
     * 포스트 삭제
     */
    @Transactional
    public void delete(Long id) {
        // 포스트 엔티티
        Post targetPost = getPostEntityById(id);

        // File 포스트 삭제
        File postFile = new File(targetPost.getObjectKey());
        postFile.delete();

        // s3에서 삭제
        awsS3Service.delete(targetPost.getObjectKey());

        // DB 포스트 삭제
        postRepository.delete(targetPost);
    }

    /**
     * 임시저장 상태의 포스트를 공개 상태로 전환
     */
    @Transactional
    public void release(Long id) {
        // 포스트 엔티티
        Post post = getPostEntityById(id);
        String oldObjectKey = post.getObjectKey();

        // 포스트 릴리스
        // 포스트가 임시 저장 상태가 아닌 경우 PostNotTempException 발생
        post.release();

        // 포스트 temp → release 폴더 이동
        File postObject = new File(oldObjectKey);
        postObject.renameTo(new File(post.getObjectKey()));

        // s3 포스트 temp → release으로 이동
        awsS3Service.move(oldObjectKey, post.getObjectKey());
    }

    /**
     * 포스트 중복 검사
     */
    private void validateDuplicatePost(String subject) {
        postRepository.findBySubject(subject).ifPresent(p -> {
            throw new PostDuplicateException();
        });
    }
}
