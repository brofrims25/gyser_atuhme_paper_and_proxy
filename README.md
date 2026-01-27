<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Web Nonton YouTube dengan Upload & Profile</title>
    <style>
        body { 
            font-family: Arial, sans-serif; 
            margin: 0; 
            padding: 20px; 
            background-color: #1a1a1a; /* Gelap dominan */
            color: #ffffff; /* Putih untuk teks */
        }
        .container { 
            max-width: 1000px; 
            margin: auto; 
            background: #2c2c2c; /* Gelap dengan sedikit cahaya */
            padding: 20px; 
            border-radius: 8px; 
            box-shadow: 0 0 10px #ff0000; /* Bayangan merah */
        }
        .header { 
            text-align: center; 
            margin-bottom: 20px; 
            color: #ff0000; /* Merah untuk header */
        }
        .nav { 
            display: flex; 
            justify-content: space-between; 
            margin-bottom: 20px; 
        }
        .nav button { 
            background: #ff0000; /* Merah dominan */
            color: #ffffff; /* Putih */
            border: none; 
            padding: 10px; 
            cursor: pointer; 
            border-radius: 5px; 
        }
        .nav button:hover { 
            background: #cc0000; /* Merah gelap saat hover */
        }
        .section { 
            display: none; 
        }
        .active { 
            display: block; 
        }
        .video-grid { 
            display: grid; 
            grid-template-columns: repeat(auto-fill, minmax(300px, 1fr)); 
            gap: 20px; 
        }
        .video-item { 
            background: #333333; /* Gelap */
            padding: 10px; 
            border-radius: 5px; 
            text-align: center; 
            border: 1px solid #ff0000; /* Border merah */
        }
        .video-item iframe { 
            width: 100%; 
            height: 200px; 
        }
        .like-btn { 
            background: #ff0000; /* Merah */
            color: white; 
            border: none; 
            padding: 5px 10px; 
            cursor: pointer; 
            border-radius: 5px; 
        }
        .like-btn.liked { 
            background: #cc0000; /* Merah gelap */
        }
        .comment-section { 
            margin-top: 10px; 
        }
        .comment { 
            margin-bottom: 5px; 
            padding: 5px; 
            background: #444444; /* Gelap */
            border-radius: 3px; 
            color: #ffffff; 
        }
        input, button, textarea { 
            margin: 5px; 
            padding: 10px; 
            width: calc(100% - 20px); 
            background: #333333; /* Gelap */
            color: #ffffff; 
            border: 1px solid #ff0000; /* Border merah */
            border-radius: 5px; 
        }
        button { 
            background: #ff0000; /* Merah */
            cursor: pointer; 
        }
        button:hover { 
            background: #cc0000; /* Merah gelap */
        }
        .profile { 
            text-align: center; 
        }
        .profile img { 
            width: 100px; 
            height: 100px; 
            border-radius: 50%; 
            background: #666666; /* Abu-abu gelap */
            border: 2px solid #ff0000; /* Border merah */
        }
        .profile input[type="file"] { 
            width: auto; 
            margin: 10px; 
        }
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <h1>Web Nonton YouTube dengan Upload & Profile</h1>
        </div>
        <div class="nav">
            <div>
                <button onclick="showSection('home')">Home</button>
                <button onclick="showSection('upload')">Upload Video</button>
                <button onclick="showSection('profile')">Profile</button>
            </div>
            <div id="auth-buttons">
                <button onclick="showSection('register')">Register</button>
                <button onclick="showSection('login')">Login</button>
            </div>
        </div>
        <!-- Home Section: Galeri Video -->
        <div id="home" class="section active">
            <h2>Galeri Video</h2>
            <div id="video-gallery" class="video-grid"></div>
        </div>
        <!-- Upload Section -->
        <div id="upload" class="section">
            <h2>Upload Video dari Link YouTube</h2>
            <input type="text" id="video-link" placeholder="Masukkan link YouTube" />
            <input type="text" id="video-title" placeholder="Judul Video" />
            <button onclick="uploadVideo()">Upload</button>
        </div>
        <!-- Register Section -->
        <div id="register" class="section">
            <h2>Register</h2>
            <input type="text" id="reg-username" placeholder="Username" />
            <input type="password" id="reg-password" placeholder="Password" />
            <button onclick="register()">Register</button>
        </div>
        <!-- Login Section -->
        <div id="login" class="section">
            <h2>Login</h2>
            <input type="text" id="login-username" placeholder="Username" />
            <input type="password" id="login-password" placeholder="Password" />
            <button onclick="login()">Login</button>
        </div>
        <!-- Profile Section -->
        <div id="profile" class="section">
            <h2>Profile</h2>
            <div class="profile">
                <img id="profile-pic" src="" alt="Profile Picture" />
                <input type="file" id="profile-pic-input" accept="image/*" onchange="previewProfilePic()" />
                <p><strong>Username:</strong> <span id="profile-username"></span></p>
                <textarea id="profile-bio" placeholder="Bio..."></textarea>
                <button onclick="saveProfile()">Save Profile</button>
                <button onclick="logout()">Logout</button>
            </div>
        </div>
    </div>
    <script>
        let currentUser = null;
        let users = JSON.parse(localStorage.getItem('users')) || [];
        let videos = JSON.parse(localStorage.getItem('videos')) || [];
        // Load data on page load
        window.onload = function() {
            if (localStorage.getItem('currentUser')) {
                currentUser = JSON.parse(localStorage.getItem('currentUser'));
                showLoggedIn();
                loadProfile();
            }
            renderGallery();
        };
        // Show section
        function showSection(sectionId) {
            document.querySelectorAll('.section').forEach(sec => sec.classList.remove('active'));
            document.getElementById(sectionId).classList.add('active');
        }
        // Register
        function register() {
            const username = document.getElementById('reg-username').value;
            const password = document.getElementById('reg-password').value;
            if (username && password) {
                const user = { username, password, bio: '', profilePic: '' };
                users.push(user);
                localStorage.setItem('users', JSON.stringify(users));
                alert('Registrasi berhasil!');
                showSection('login');
            } else {
                alert('Isi semua field!');
            }
        }
        // Login
        function login() {
            const username = document.getElementById('login-username').value;
            const password = document.getElementById('login-password').value;
            const user = users.find(u => u.username === username && u.password === password);
            if (user) {
                currentUser = user;
                localStorage.setItem('currentUser', JSON.stringify(user));
                showLoggedIn();
                loadProfile();
                showSection('home');
            } else {
                alert('Username atau password salah!');
            }
        }
        // Logout
        function logout() {
            currentUser = null;
            localStorage.removeItem('currentUser');
            showLoggedOut();
            showSection('home');
        }
        // Show logged in/out buttons
        function showLoggedIn() {
            document.getElementById('auth-buttons').innerHTML = `<button onclick="showSection('profile')">Profile</button> <button onclick="logout()">Logout</button>`;
        }
        function showLoggedOut() {
            document.getElementById('auth-buttons').innerHTML = `<button onclick="showSection('register')">Register</button> <button onclick="showSection('login')">Login</button>`;
        }
        // Upload Video
        function uploadVideo() {
            if (!currentUser) {
                alert('Harus login untuk upload!');
                return;
            }
            const link = document.getElementById('video-link').value;
            const title = document.getElementById('video-title').value;
            const videoId = extractVideoId(link);
            if (videoId && title) {
                const video = { id: videoId, title, user: currentUser.username, likes: 0, comments: [] };
                videos.push(video);
                localStorage.setItem('videos', JSON.stringify(videos));
                renderGallery();
                showSection('home');
            } else {
                alert('Link atau judul tidak valid!');
            }
        }
        // Extract Video ID
        function extractVideoId(url) {
            const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:.*v=|.*\/|.*embed\/|.*v\/))([^&\n?#]+)/);
            return match ? match[1] : null;
        }
        // Render Gallery
        function renderGallery() {
            const gallery = document.getElementById('video-gallery');
            gallery.innerHTML = '';
            videos.forEach((video, index) => {
                const div = document.createElement('div');
                div.className = 'video-item';
                div.innerHTML = `
                    <h3>${video.title}</h3>
                    <iframe src="https://www.youtube.com/embed/${video.id}" allowfullscreen></iframe>
                    <p>Uploaded by: ${video.user}</p>
                    <button class="like-btn" onclick="toggleLike(${index})">Like (${video.likes})</button>
                    <div class="comment-section">
                        <input type="text" id="comment-${index}" placeholder="Komentar..." />
                        <button onclick="addComment(${index})">Kirim</button>
                        <div id="comments-${index}"></div>
                    </div>
                `;
                gallery.appendChild(div);
                renderComments(index);
            });
        }
        // Toggle Like
        function toggleLike(index) {
            if (!currentUser) {
                alert('Harus login untuk like!');
                return;
            }
            videos[index].likes += 1; // Sederhana, tanpa toggle per user
            localStorage.setItem('videos', JSON.stringify(videos));
            renderGallery();
        }
        // Add Comment
        function addComment(index) {
            if (!currentUser) {
                alert('Harus login untuk komentar!');
                return;
            }
            const commentText = document.getElementById(`comment-${index}`).value;
            if (commentText) {
                videos[index].comments.push({ user: currentUser.username, text: commentText });
                localStorage.setItem('videos', JSON.stringify(videos));
                document.getElementById(`comment-${index}`).value = '';
                renderComments(index);
            }
        }
        // Render Comments
        function renderComments(index) {
            const commentsDiv = document.getElementById(`comments-${index}`);
            commentsDiv.innerHTML = '';
            videos[index].comments.forEach(comment => {
                const div = document.createElement('div');
                div.className = 'comment';
                div.textContent = `${comment.user}: ${comment.text}`;
                commentsDiv.appendChild(div);
            });
        }
        // Load Profile
        function loadProfile() {
            if (currentUser) {
                document.getElementById('profile-username').textContent = currentUser.username;
                document.getElementById('profile-bio').value = currentUser.bio || '';
                document.getElementById('profile-pic').src = currentUser.profilePic || '';
            }
        }
        // Preview Profile Pic
        function previewProfilePic() {
            const file = document.getElementById('profile-pic-input').files[0];
            if (file) {
                const reader = new FileReader();
                reader.onload = function(e) {
                    document.getElementById('profile-pic').src = e.target.result;
                };
                reader.readAsDataURL(file);
            }
        }
        // Save Profile
        function saveProfile() {
            if (currentUser) {
                currentUser.bio = document.getElementById('profile-bio').value;
                currentUser.profilePic = document.getElementById('profile-pic').src;
                const userIndex = users.findIndex(u => u.username === currentUser.username);
                users[userIndex] = currentUser;
                localStorage.setItem('users', JSON.stringify(users));
                localStorage.setItem('currentUser', JSON.stringify(currentUser));
                alert('Profile disimpan!');
            }
        }
    </script>
</body>
</html>
