<!DOCTYPE html>
<html lang="id">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Web Nonton YouTube Sederhana</title>
    <style>
        body { font-family: Arial, sans-serif; margin: 20px; background-color: #f4f4f4; }
        .container { max-width: 800px; margin: auto; background: white; padding: 20px; border-radius: 8px; box-shadow: 0 0 10px rgba(0,0,0,0.1); }
        .video-container { margin-bottom: 20px; }
        iframe { width: 100%; height: 400px; border: none; }
        .login-section { margin-bottom: 20px; }
        .comment-section { margin-top: 20px; }
        .comment { margin-bottom: 10px; padding: 10px; background: #f9f9f9; border-radius: 5px; }
        .like-btn { background: #007bff; color: white; border: none; padding: 10px; cursor: pointer; border-radius: 5px; }
        .like-btn.liked { background: #28a745; }
        input, button { margin: 5px; padding: 10px; }
        .upload-section { margin-bottom: 20px; }
    </style>
</head>
<body>
    <div class="container">
        <h1>Web Nonton YouTube</h1>
        
        <!-- Sistem Login -->
        <div class="login-section">
            <h2>Login</h2>
            <input type="text" id="username" placeholder="Masukkan username" />
            <button onclick="login()">Login</button>
            <button onclick="logout()">Logout</button>
            <p id="user-status">Belum login</p>
        </div>
        
        <!-- Upload Video dari Link YouTube -->
        <div class="upload-section">
            <h2>Upload Video dari Link YouTube</h2>
            <input type="text" id="video-link" placeholder="Masukkan link YouTube (contoh: https://youtu.be/lLJGB2fcZxY)" value="https://youtu.be/lLJGB2fcZxY?si=LIWhi1wt00PnbggY" />
            <button onclick="loadVideo()">Load Video</button>
        </div>
        
        <!-- Video Player -->
        <div class="video-container">
            <h2>Video Player</h2>
            <iframe id="video-player" src="https://www.youtube.com/embed/lLJGB2fcZxY" allowfullscreen></iframe>
        </div>
        
        <!-- Like Button -->
        <div>
            <button id="like-btn" class="like-btn" onclick="toggleLike()">Like (0)</button>
        </div>
        
        <!-- Komentar Section -->
        <div class="comment-section">
            <h2>Komentar</h2>
            <input type="text" id="comment-input" placeholder="Tulis komentar..." />
            <button onclick="addComment()">Kirim Komentar</button>
            <div id="comments-list"></div>
        </div>
    </div>

    <script>
        let isLoggedIn = false;
        let username = '';
        let likes = 0;
        let isLiked = false;
        let comments = [];

        // Load data dari localStorage
        window.onload = function() {
            if (localStorage.getItem('username')) {
                username = localStorage.getItem('username');
                isLoggedIn = true;
                document.getElementById('user-status').textContent = `Logged in as: ${username}`;
            }
            likes = parseInt(localStorage.getItem('likes')) || 0;
            isLiked = localStorage.getItem('isLiked') === 'true';
            comments = JSON.parse(localStorage.getItem('comments')) || [];
            updateUI();
        };

        // Fungsi Login
        function login() {
            username = document.getElementById('username').value;
            if (username) {
                isLoggedIn = true;
                localStorage.setItem('username', username);
                document.getElementById('user-status').textContent = `Logged in as: ${username}`;
            }
        }

        // Fungsi Logout
        function logout() {
            isLoggedIn = false;
            username = '';
            localStorage.removeItem('username');
            document.getElementById('user-status').textContent = 'Belum login';
        }

        // Fungsi Load Video dari Link
        function loadVideo() {
            const link = document.getElementById('video-link').value;
            const videoId = extractVideoId(link);
            if (videoId) {
                document.getElementById('video-player').src = `https://www.youtube.com/embed/${videoId}`;
            } else {
                alert('Link YouTube tidak valid!');
            }
        }

        // Ekstrak Video ID dari Link YouTube
        function extractVideoId(url) {
            const match = url.match(/(?:youtu\.be\/|youtube\.com\/(?:.*v=|.*\/|.*embed\/|.*v\/))([^&\n?#]+)/);
            return match ? match[1] : null;
        }

        // Fungsi Toggle Like
        function toggleLike() {
            if (!isLoggedIn) {
                alert('Harus login untuk like!');
                return;
            }
            isLiked = !isLiked;
            likes += isLiked ? 1 : -1;
            localStorage.setItem('likes', likes);
            localStorage.setItem('isLiked', isLiked);
            updateUI();
        }

        // Fungsi Add Comment
        function addComment() {
            if (!isLoggedIn) {
                alert('Harus login untuk komentar!');
                return;
            }
            const commentText = document.getElementById('comment-input').value;
            if (commentText) {
                comments.push({ user: username, text: commentText });
                localStorage.setItem('comments', JSON.stringify(comments));
                document.getElementById('comment-input').value = '';
                updateUI();
            }
        }

        // Update UI
        function updateUI() {
            document.getElementById('like-btn').textContent = `Like (${likes})`;
            document.getElementById('like-btn').classList.toggle('liked', isLiked);
            const commentsList = document.getElementById('comments-list');
            commentsList.innerHTML = '';
            comments.forEach(comment => {
                const div = document.createElement('div');
                div.className = 'comment';
                div.textContent = `${comment.user}: ${comment.text}`;
                commentsList.appendChild(div);
            });
        }
    </script>
</body>
</html>
