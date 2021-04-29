const createSelectOption = (name) => {
  target = document.querySelector("#category");
  const option = `
        <option value="${name}">${name}</option>
    `;

  target.insertAdjacentHTML("beforeend", option);
};

const getCategories = async () => {
  const response = await fetch("/category")
    .then((response) => response.json())
    .then((response) => {
      response.map((name) => {
        createSelectOption(name);
      });
    });
};

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

  const response = await fetch("/posts/upload", {
    method: "POST",
    headers: {
      "Content-Type": "application/json;charset=utf-8",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) alert("포스트 업로드 성공");
  else alert("포스트 업로드 실패");
};

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

  const response = await fetch("/posts/upload", {
    method: "POST",
    headers: {
      "Content-Type": "application/json;charset=utf-8",
    },
    body: JSON.stringify(data),
  });

  if (response.ok) alert("포스트 업로드 성공");
  else alert("포스트 업로드 실패");
};

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

getCategories();
