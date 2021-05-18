const createCategoryBtn = async () => {
  const category = document.querySelector("#category");
  const data = {
    name: category.value,
  };

  const response = await fetch("/admin/blog/category", {
    method: "post",
    headers: {
      "Content-Type": "application/json;charset=utf-8",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) {
    alert("카테고리 생성 성공");
    category.value = "";
  } else alert("포스트 업로드 실패");
};

const createTableRow = (idx, post) => {
  const target = document.querySelector("#post-list");
  let status = "info";
  if (post.status === "TEMP") status = "warning";

  const tr = `
        <tr>
          <th scope="row" style="text-align: center">${idx + 1}</th>
          <td>${post.category}</td>
          <td>${post.subject}</td>
          <td style="text-align: center">${post.uploadDate}</td>
          <td style="text-align: center"><button type="button" class="btn btn-${status} btn-sm">${
    post.status
  }</button></td>
          <td style="text-align: center"><button type="button" class="btn btn-success btn-sm">Modify</button></td>
          <td style="text-align: center"><button type="button" class="btn btn-danger btn-sm">Delete</button></td>
        </tr>
    `;

  target.insertAdjacentHTML("beforeend", tr);
};

const getPostList = async () => {
  const response = await fetch("/admin/blog/posts")
    .then((response) => response.json())
    .then((response) => {
      response.map((post, index) => {
        createTableRow(index, post);
      });
    });
};

getPostList();
