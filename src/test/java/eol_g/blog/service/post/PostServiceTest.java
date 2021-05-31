package eol_g.blog.service.post;

import eol_g.blog.domain.Category;
import eol_g.blog.domain.Post;
import eol_g.blog.domain.PostStatus;

import java.util.ArrayList;
import java.util.List;

public class PostServiceTest {
    public Post createTestPost(Long id, String categoryName, String subject, PostStatus status) {
        Category category = Category.createCategory(categoryName);
        String objectKey = createObjectKey(status, categoryName, subject);
        return Post.builder()
                .id(id)
                .category(category)
                .subject(subject)
                .filePath(objectKey)
                .s3Key(objectKey)
                .status(status)
                .build();
    }

    public List<Post> createTestPostList(String categoryName) {
        List<Post> testPostList = new ArrayList<>();
        String otherCategoryName = "other_category";
        String subject = "subject";
        for (Long id = 1L; id < 100; id++) {
            Post testPost;

            if (id % 4 == 0) testPost = createTestPost(id, categoryName, subject, PostStatus.TEMP);
            else if (id % 3 == 0) testPost = createTestPost(id, otherCategoryName, subject, PostStatus.RELEASE);
            else testPost = createTestPost(id, categoryName, subject, PostStatus.RELEASE);

            testPostList.add(testPost);
        }

        return testPostList;
    }

    public String createObjectKey(PostStatus status, String categoryName, String subject) {
        return "test/" + status.toString().toLowerCase() + "/" + categoryName + "/" + subject + ".md";
    }
}
