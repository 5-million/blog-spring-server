const deletePost = async (id) => {
  answer = confirm("정말 삭제하시겠습니까 ?");

  if (!answer) return;

  const response = await fetch(`/admin/blog/posts/${id}`, {
    method: "DELETE",
  });

  if (response.ok) {
    alert("포스트 삭제 성공");
    location.reload();
  }
  else alert("포스트 삭제 실패");
};

