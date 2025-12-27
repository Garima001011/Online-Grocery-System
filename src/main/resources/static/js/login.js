function login() {
  const email = document.getElementById("email").value.trim();
  const password = document.getElementById("password").value;

  fetch("/api/auth/login", {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ email, password })
  })
    .then(async res => {
      if (!res.ok) {
        const t = await res.text();
        throw new Error(t || "Login failed");
      }
      return res.json();
    })
    .then(user => {
      localStorage.setItem("user", JSON.stringify(user));

      if (user.role === "ADMIN") {
        window.location.href = "/admin.html";
      } else {
        window.location.href = "/shop.html";
      }
    })
    .catch(err => {
      document.getElementById("msg").innerText = "Invalid login";
    });
}
