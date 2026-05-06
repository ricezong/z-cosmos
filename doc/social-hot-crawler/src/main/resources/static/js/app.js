/**
 * 社交媒体热点聚合 - 前端逻辑
 */
const API = '/api';
let platforms = [];
let currentPlatform = 'all';
let currentTopicId = null;

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', () => {
    initTabs();
    initSearch();
    loadPlatforms();
    loadDashboard();
});

// ==================== Tab切换 ====================
function initTabs() {
    document.querySelectorAll('.tab').forEach(tab => {
        tab.addEventListener('click', () => switchTab(tab.dataset.tab));
    });
}

function switchTab(name) {
    document.querySelectorAll('.tab').forEach(t => t.classList.remove('active'));
    document.querySelector(`.tab[data-tab="${name}"]`).classList.add('active');
    document.querySelectorAll('.tab-content').forEach(c => c.classList.remove('active'));
    document.getElementById(`tab-${name}`).classList.add('active');
    if (name === 'dashboard') loadDashboard();
    if (name === 'hot') loadHotTopics();
    if (name === 'platforms') loadPlatformDetails();
}

// ==================== 全局搜索 ====================
function initSearch() {
    const input = document.getElementById('globalSearch');
    let timer;
    input.addEventListener('input', () => {
        clearTimeout(timer);
        timer = setTimeout(() => {
            const q = input.value.trim();
            if (q.length >= 2) doGlobalSearch(q);
        }, 500);
    });
    input.addEventListener('keydown', (e) => {
        if (e.key === 'Enter') {
            const q = input.value.trim();
            if (q.length >= 2) doGlobalSearch(q);
        }
    });
}

async function doGlobalSearch(q) {
    switchTab('hot');
    try {
        const res = await apiGet(`/topics/search?q=${encodeURIComponent(q)}`);
        renderTopicList('hotTopicList', res);
        showToast(`搜索到 ${res.length} 条结果`, 'info');
    } catch (e) {
        showToast('搜索失败', 'error');
    }
}

// ==================== 平台管理 ====================
async function loadPlatforms() {
    try {
        platforms = await apiGet('/platforms');
        renderPlatformFilters();
    } catch (e) {
        console.error('加载平台失败', e);
    }
}

function renderPlatformFilters() {
    const container = document.getElementById('platformFilters');
    let html = '<button class="filter-btn active" data-platform="all" onclick="selectPlatform(\'all\',this)">全部</button>';
    platforms.forEach(p => {
        html += `<button class="filter-btn" data-platform="${p.platformId}" onclick="selectPlatform('${p.platformId}',this)">${p.icon} ${p.platformName}</button>`;
    });
    container.innerHTML = html;
}

function selectPlatform(pid, btn) {
    currentPlatform = pid;
    document.querySelectorAll('.filter-btn').forEach(b => b.classList.remove('active'));
    btn.classList.add('active');
    loadHotTopics();
}

// ==================== 总览页 ====================
async function loadDashboard() {
    try {
        const [stats, plats] = await Promise.all([
            apiGet('/platforms/statistics'),
            apiGet('/platforms')
        ]);
        platforms = plats;

        document.getElementById('statTopics').textContent = stats.totalTopics || 0;
        document.getElementById('statComments').textContent = stats.totalComments || 0;
        document.getElementById('statPlatforms').textContent = stats.platformCount || 0;
        document.getElementById('statTasks').textContent = stats.totalTasks || 0;

        renderPlatformGrid(plats);

        // 最新热点
        const topics = await apiGet('/topics');
        renderTopicList('latestTopics', topics.slice(0, 10));

        // 最近任务
        const tasks = await apiGet('/crawl/tasks');
        renderTaskList(tasks);

        renderPlatformFilters();
    } catch (e) {
        console.error('加载总览失败', e);
    }
}

function renderPlatformGrid(plats) {
    const grid = document.getElementById('platformGrid');
    if (!plats.length) {
        grid.innerHTML = '<div class="empty-state">暂无已注册平台</div>';
        return;
    }
    grid.innerHTML = plats.map(p => `
        <div class="platform-card" onclick="selectPlatformAndGo('${p.platformId}')">
            <div class="icon">${p.icon}</div>
            <div class="name">${p.platformName}</div>
            <div class="count">${p.topicCount} 条热点</div>
            <div class="crawl-btn">
                <button class="btn btn-sm btn-primary" onclick="event.stopPropagation();crawlPlatform('${p.platformId}')">爬取</button>
            </div>
        </div>
    `).join('');
}

function renderTaskList(tasks) {
    const el = document.getElementById('recentTasks');
    if (!tasks.length) {
        el.innerHTML = '<div class="empty-state">暂无任务记录</div>';
        return;
    }
    el.innerHTML = tasks.map(t => `
        <div class="task-item">
            <span class="task-status ${t.status}">${t.status}</span>
            <span>${getPlatformName(t.platform)}</span>
            <span>${t.taskType === 'HOT_TOPICS' ? '热点' : '评论'}</span>
            <span>${t.fetchedCount || 0} 条</span>
            <span style="margin-left:auto;color:var(--text-light)">${t.duration ? (t.duration/1000).toFixed(1)+'s' : '-'}</span>
        </div>
    `).join('');
}

function selectPlatformAndGo(pid) {
    currentPlatform = pid;
    switchTab('hot');
    document.querySelectorAll('.filter-btn').forEach(b => {
        b.classList.toggle('active', b.dataset.platform === pid);
    });
    loadHotTopics();
}

// ==================== 热点页 ====================
async function loadHotTopics() {
    const url = currentPlatform === 'all' ? '/topics' : `/topics?platform=${currentPlatform}`;
    try {
        const topics = await apiGet(url);
        renderTopicList('hotTopicList', topics);
    } catch (e) {
        showToast('加载热点失败', 'error');
    }
}

function renderTopicList(containerId, topics) {
    const el = document.getElementById(containerId);
    if (!topics || !topics.length) {
        el.innerHTML = '<div class="empty-state">暂无热点数据，请先爬取</div>';
        return;
    }
    el.innerHTML = topics.map((t, i) => `
        <div class="topic-item" onclick="viewTopicComments(${t.id})">
            <div class="topic-rank ${i < 3 ? 'top3' : ''}">${t.rank || (i + 1)}</div>
            ${t.coverUrl ? `<img class="topic-cover" src="${esc(t.coverUrl)}" onerror="this.style.display='none'" alt="">` : ''}
            <div class="topic-body">
                <div class="topic-title">
                    <span class="platform-badge ${t.platform}">${getPlatformName(t.platform)}</span>
                    ${esc(t.title)}
                    ${t.heatLabel ? `<span class="heat-label ${getHeatClass(t.heatLabel)}">${esc(t.heatLabel)}</span>` : ''}
                </div>
                ${t.summary ? `<div class="topic-summary">${esc(t.summary)}</div>` : ''}
                <div class="topic-meta">
                    ${t.author ? `<span>&#128100; ${esc(t.author)}</span>` : ''}
                    ${t.commentCount ? `<span>&#128172; ${formatNum(t.commentCount)}</span>` : ''}
                    ${t.likeCount ? `<span>&#10084; ${formatNum(t.likeCount)}</span>` : ''}
                    ${t.viewCount ? `<span>&#128065; ${formatNum(t.viewCount)}</span>` : ''}
                </div>
            </div>
            <div class="topic-heat">${formatNum(t.heatValue)}</div>
        </div>
    `).join('');
}

// ==================== 评论页 ====================
async function viewTopicComments(topicId) {
    currentTopicId = topicId;
    switchTab('comments');

    try {
        const topic = await apiGet(`/topics/${topicId}`);
        document.getElementById('commentTopicCard').style.display = 'block';
        document.getElementById('commentPlatform').className = `platform-badge ${topic.platform}`;
        document.getElementById('commentPlatform').textContent = getPlatformName(topic.platform);
        document.getElementById('commentTitle').textContent = topic.title;
        document.getElementById('commentSummary').textContent = topic.summary || '';
        document.getElementById('commentHeat').innerHTML = `&#128293; 热度 ${formatNum(topic.heatValue)}`;
        document.getElementById('commentAuthor').innerHTML = topic.author ? `&#128100; ${esc(topic.author)}` : '';
        document.getElementById('crawlCommentBtn').onclick = () => crawlTopicComments();

        const comments = await apiGet(`/topics/${topicId}/comments`);
        renderCommentList(comments);
    } catch (e) {
        showToast('加载话题详情失败', 'error');
    }
}

function renderCommentList(comments) {
    const el = document.getElementById('commentList');
    if (!comments || !comments.length) {
        el.innerHTML = '<div class="empty-state">暂无评论，点击"爬取评论"获取数据</div>';
        return;
    }

    // 分离顶级评论和回复
    const topComments = comments.filter(c => !c.parentId);
    const replies = comments.filter(c => c.parentId);

    let html = '';
    topComments.forEach(c => {
        html += renderCommentItem(c);
        // 找到该评论的回复
        const cReplies = replies.filter(r => r.parentId === c.id);
        cReplies.forEach(r => {
            html += renderCommentItem(r, true);
        });
    });
    el.innerHTML = html;
}

function renderCommentItem(c, isReply = false) {
    const initial = (c.username || '?')[0];
    return `
        <div class="comment-item ${isReply ? 'reply' : ''}">
            <div class="comment-avatar">${initial}</div>
            <div class="comment-body">
                <div class="comment-header">
                    <span class="comment-username">${esc(c.username || '匿名')}</span>
                    <span class="comment-time">${formatTime(c.publishedAt)}</span>
                    ${c.floor ? `<span class="comment-floor">#${c.floor}</span>` : ''}
                </div>
                <div class="comment-content">${esc(c.content)}</div>
                <div class="comment-actions">
                    <span>&#10084; ${formatNum(c.likeCount)}</span>
                    ${c.replyCount ? `<span>&#128172; ${c.replyCount} 回复</span>` : ''}
                </div>
            </div>
        </div>
    `;
}

// ==================== 平台管理页 ====================
async function loadPlatformDetails() {
    try {
        const plats = await apiGet('/platforms');
        const el = document.getElementById('platformDetailList');
        el.innerHTML = plats.map(p => `
            <div class="platform-detail">
                <div class="platform-detail-header">
                    <span class="icon">${p.icon}</span>
                    <span class="name">${p.platformName}</span>
                    <span class="id">${p.platformId}</span>
                </div>
                <div class="platform-detail-body">
                    <div class="item"><div class="label">状态</div><div class="value">${p.status ? p.status.healthy ? '&#9989; 正常' : '&#10060; 异常' : '-'}</div></div>
                    <div class="item"><div class="label">热点数</div><div class="value">${p.topicCount}</div></div>
                    <div class="item"><div class="label">评论数</div><div class="value">${p.commentCount}</div></div>
                    <div class="item"><div class="label">支持功能</div><div class="value">${(p.capabilities || []).join(', ')}</div></div>
                </div>
            </div>
        `).join('');
    } catch (e) {
        showToast('加载平台详情失败', 'error');
    }
}

// ==================== 爬取操作 ====================
async function crawlPlatform(pid) {
    showToast(`正在爬取 ${getPlatformName(pid)} 热点...`, 'info');
    try {
        const res = await apiPost(`/crawl/hot-topics/${pid}`);
        if (res.success) {
            showToast(`${getPlatformName(pid)} 爬取完成，获取 ${res.fetchedCount} 条热点`, 'success');
            loadDashboard();
        } else {
            showToast(`爬取失败: ${res.error}`, 'error');
        }
    } catch (e) {
        showToast('爬取请求失败', 'error');
    }
}

async function crawlAllPlatforms() {
    showToast('正在爬取所有平台热点...', 'info');
    try {
        const res = await apiPost('/crawl/hot-topics');
        if (res.success) {
            showToast(`全部完成！共获取 ${res.totalFetched} 条热点`, 'success');
            loadDashboard();
        } else {
            showToast(`爬取失败: ${res.error}`, 'error');
        }
    } catch (e) {
        showToast('爬取请求失败', 'error');
    }
}

async function crawlSelectedPlatform() {
    const pid = currentPlatform === 'all' ? null : currentPlatform;
    if (pid) {
        await crawlPlatform(pid);
    } else {
        await crawlAllPlatforms();
    }
}

async function crawlTopicComments() {
    if (!currentTopicId) return;
    showToast('正在爬取评论...', 'info');
    try {
        const res = await apiPost(`/crawl/comments/${currentTopicId}`);
        if (res.success) {
            showToast(`评论爬取完成，获取 ${res.fetchedCount} 条评论`, 'success');
            const comments = await apiGet(`/topics/${currentTopicId}/comments`);
            renderCommentList(comments);
        } else {
            showToast(`爬取失败: ${res.error}`, 'error');
        }
    } catch (e) {
        showToast('爬取请求失败', 'error');
    }
}

// ==================== API封装 ====================
async function apiGet(path) {
    const res = await fetch(`${API}${path}`);
    return res.json();
}

async function apiPost(path) {
    const res = await fetch(`${API}${path}`, { method: 'POST' });
    return res.json();
}

// ==================== 工具函数 ====================
function esc(str) {
    if (!str) return '';
    const d = document.createElement('div');
    d.textContent = str;
    return d.innerHTML;
}

function formatNum(n) {
    if (n == null) return '0';
    if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿';
    if (n >= 10000) return (n / 10000).toFixed(1) + '万';
    return n.toLocaleString();
}

function formatTime(dt) {
    if (!dt) return '';
    const d = new Date(dt);
    const now = new Date();
    const diff = (now - d) / 1000;
    if (diff < 60) return '刚刚';
    if (diff < 3600) return Math.floor(diff / 60) + '分钟前';
    if (diff < 86400) return Math.floor(diff / 3600) + '小时前';
    return Math.floor(diff / 86400) + '天前';
}

function getPlatformName(pid) {
    const p = platforms.find(x => x.platformId === pid);
    return p ? p.platformName : pid;
}

function getHeatClass(label) {
    if (!label) return '';
    const l = label.toLowerCase();
    if (l.includes('爆') || l.includes('沸')) return 'burst';
    if (l.includes('热')) return 'hot';
    if (l.includes('新')) return 'new';
    return 'hot';
}

function showToast(msg, type = 'info') {
    const t = document.getElementById('toast');
    t.textContent = msg;
    t.className = `toast ${type} show`;
    setTimeout(() => t.classList.remove('show'), 3000);
}
