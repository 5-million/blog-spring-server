/**
 * 포스트 카테고리 select에 옵션 추가
 */
const createSelectOption = (name) => {
  target = document.querySelector("#category");
  const option = `
        <option value="${name}">${name}</option>
    `;

  target.insertAdjacentHTML("beforeend", option);
};

const getCategories = () => {
  fetch("/api/blog/category")
    .then((response) => response.json())
    .then((response) => {
      response.map((name) => {
        createSelectOption(name);
      });
    });
};

/**
 * 포스트 업로드 버튼 클릭 이벤트 핸들러
 */
const uploadPostBtn = async () => {
  const subject = document.querySelector("#subject").value;
  const category = document.querySelector("#category").value;
  const status = "PUBLIC";
  const content = editor.getMarkdown();

  const data = {
    subject,
    category,
    status,
    content,
  };

  const response = await fetch("/admin/blog/posts", {
    method: "POST",
    headers: {
      "Content-Type": "application/json;charset=utf-8",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) alert("포스트 업로드 성공");
  else alert("포스트 업로드 실패");
};

/**
 * 포스트 임시 저장 버튼 클릭 이벤트 핸들러
 */
const savePostBtn = async () => {
  const subject = document.querySelector("#subject").value;
  const category = document.querySelector("#category").value;
  const status = "TEMP";
  const content = editor.getMarkdown();

  const data = {
    subject,
    category,
    status,
    content,
  };

  const response = await fetch("/admin/blog/posts", {
    method: "POST",
    headers: {
      "Content-Type": "application/json;charset=utf-8",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) alert("포스트 업로드 성공");
  else alert("포스트 업로드 실패");
};

/**
 * 업로드된 이미지 리스트에 추가
 */
const putUploadImageUrl = ({ filename, url }) => {
  const target = document.querySelector(`#img-path-ul-${uploadImageCount}`);
  const li = `
  <li>
    <i class="far fa-copy" onclick="copyImageUrl('${url}')"></i>
    <a href="${url}" target="_blank">${filename}</a>
  </li>
  `;

  target.insertAdjacentHTML("beforeend", li);

  uploadImageCount += 1;
  uploadImageCount %= 2;
};

/**
 * 이미지 업로드 버튼 이벤트 핸들러
 */
const imageUploadBtn = () => {
  const file = document.querySelector("#upload-img").files[0];
  const formData = new FormData();
  formData.append("file", file);

  fetch("/admin/blog/img/upload", {
    method: "post",
    headers: {},
    body: formData,
  })
    .then((response) => {
      if (response.ok) return response.json();
      else throw new Error("이미지 업로드 실패");
    })
    .then((response) => putUploadImageUrl(response))
    .catch((err) => alert(err));
};

/**
 * 업로드된 이미지 url 클립보드에 복사하는 이벤트 핸들러
 */
const copyImageUrl = (url) => {
  const createTextarea = document.createElement("textarea");
  createTextarea.value = url;
  document.body.appendChild(createTextarea);

  createTextarea.select();
  document.execCommand("copy");
  document.body.removeChild(createTextarea);
};

let uploadImageCount = 0;
getCategories();

/**
 * tui editor 설정
 */
const Editor = toastui.Editor;
const editor = new Editor({
  el: document.querySelector("#editor"),
  height: "750px",
  initialEditType: "markdown",
  previewStyle: "vertical",
});

const WINDOW_OUTER_WIDTH = window.outerWidth;

if (WINDOW_OUTER_WIDTH <= 400) {
  editor.changePreviewStyle("tab");
  editor.height("550px");
} else if (WINDOW_OUTER_WIDTH <= 1024) {
  editor.height("635px");
} else {
  editor.height("800px");
}
