/**
 * 小型搜索引擎 - 前端JavaScript
 * 对应博客中的完整系统：搜集、分析、索引、查询
 */

const API_BASE = '/api';

// ==================== 页面导航 ====================

document.querySelectorAll('.nav-link').forEach(link => {
    link.addEventListener('click', (e) => {
        e.preventDefault();
        const page = link.dataset.page;
        switchPage(page);
    });
});

function switchPage(page) {
    // 更新导航高亮
    document.querySelectorAll('.nav-link').forEach(l => l.classList.remove('active'));
    document.querySelector(`.nav-link[data-page="${page}"]`).classList.add('active');

    // 切换页面
    document.querySelectorAll('.page').forEach(p => p.classList.remove('active'));
    document.getElementById(`page-${page}`).classList.add('active');

    // 加载页面数据
    if (page === 'crawl') loadCrawlData();
    if (page === 'analysis') loadAnalysisData();
    if (page === 'index') loadIndexData();
    if (page === 'stats') loadStats();
}

// ==================== Toast通知 ====================

function showToast(message, type = 'info') {
    const toast = document.getElementById('toast');
    toast.textContent = message;
    toast.className = `toast ${type} show`;
    setTimeout(() => toast.classList.remove('show'), 3000);
}

// ==================== API请求封装 ====================

async function apiGet(path) {
    const res = await fetch(`${API_BASE}${path}`);
    return res.json();
}

async function apiPost(path, body = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
    });
    return res.json();
}

// ==================== 搜索功能 ====================

const searchInput = document.getElementById('searchInput');
const searchBtn = document.getElementById('searchBtn');

searchBtn.addEventListener('click', doSearch);
searchInput.addEventListener('keypress', (e) => {
    if (e.key === 'Enter') doSearch();
});

async function doSearch() {
    const query = searchInput.value.trim();
    if (!query) {
        showToast('请输入搜索关键词', 'error');
        return;
    }

    const resultsDiv = document.getElementById('searchResults');
    resultsDiv.innerHTML = '<div class="loading"><div class="spinner"></div><p>搜索中...</p></div>';

    try {
        const data = await apiGet(`/search?q=${encodeURIComponent(query)}&limit=20`);
        renderSearchResults(data);
    } catch (err) {
        resultsDiv.innerHTML = '<div class="no-results"><div class="icon">&#9888;</div><p>搜索失败，请检查后端服务是否启动</p></div>';
        showToast('搜索请求失败', 'error');
    }
}

function renderSearchResults(data) {
    const resultsDiv = document.getElementById('searchResults');

    if (!data.results || data.results.length === 0) {
        resultsDiv.innerHTML = `
            <div class="no-results">
                <div class="icon">&#128533;</div>
                <p>未找到与 "${data.query}" 相关的结果</p>
                <p style="font-size:13px;margin-top:8px;color:#999;">请尝试其他关键词，或先爬取并索引一些网页</p>
            </div>`;
        return;
    }

    let html = `<p style="color:#64748B;font-size:14px;margin-bottom:16px;">找到约 ${data.totalResults} 条结果</p>`;
    data.results.forEach(r => {
        html += `
            <div class="result-item">
                <div class="result-url">${escapeHtml(r.url)}</div>
                <div class="result-title">${escapeHtml(r.title || '无标题')}</div>
                <div class="result-snippet">${escapeHtml(r.snippet || '')}</div>
                <div class="result-meta">
                    <span>相关度: ${(r.score * 100).toFixed(1)}%</span>
                    <span class="matched-terms">匹配词: ${escapeHtml(r.matchedTerms || '')}</span>
                </div>
            </div>`;
    });

    resultsDiv.innerHTML = html;
}

// ==================== 搜集功能 ====================

document.getElementById('addUrlBtn').addEventListener('click', async () => {
    const url = document.getElementById('crawlUrl').value.trim();
    if (!url) { showToast('请输入URL', 'error'); return; }

    try {
        const data = await apiPost('/crawl/add', { url });
        showToast(data.message, data.success ? 'success' : 'error');
        if (data.success) document.getElementById('crawlUrl').value = '';
        loadCrawlData();
    } catch (err) {
        showToast('添加URL失败', 'error');
    }
});

document.getElementById('startCrawlBtn').addEventListener('click', async () => {
    const maxPages = parseInt(document.getElementById('maxPages').value) || 10;
    showToast('开始爬取...', 'info');

    try {
        const data = await apiPost(`/crawl/start?maxPages=${maxPages}`);
        showToast(data.message, 'success');
        loadCrawlData();
    } catch (err) {
        showToast('爬取失败', 'error');
    }
});

document.getElementById('stopCrawlBtn').addEventListener('click', async () => {
    await apiPost('/crawl/stop');
    showToast('爬取已停止', 'info');
});

async function loadCrawlData() {
    try {
        const [stats, pending, pages] = await Promise.all([
            apiGet('/crawl/statistics'),
            apiGet('/crawl/pending'),
            apiGet('/crawl/pages')
        ]);

        document.getElementById('queueCount').textContent = stats.pendingUrls || 0;
        document.getElementById('crawledCount').textContent = stats.totalPages || 0;

        // 渲染待爬取队列
        const queueDiv = document.getElementById('crawlQueue');
        if (pending && pending.length > 0) {
            queueDiv.innerHTML = pending.map(u => `
                <div class="data-item">
                    <span class="url">${escapeHtml(u.url)}</span>
                    <span class="status status-pending">待爬取</span>
                </div>`).join('');
        } else {
            queueDiv.innerHTML = '<p class="empty-text">暂无待爬取的URL</p>';
        }

        // 渲染已爬取网页
        const pagesDiv = document.getElementById('crawledPages');
        if (pages && pages.length > 0) {
            pagesDiv.innerHTML = pages.slice(0, 50).map(p => `
                <div class="data-item">
                    <span class="url" title="${escapeHtml(p.url)}">${escapeHtml(p.title || p.url)}</span>
                    <span class="status status-done">${p.status}</span>
                </div>`).join('');
        } else {
            pagesDiv.innerHTML = '<p class="empty-text">暂无已爬取的网页</p>';
        }
    } catch (err) {
        console.error('加载爬取数据失败', err);
    }
}

// ==================== 分析功能 ====================

document.getElementById('analyzeAllBtn').addEventListener('click', async () => {
    showToast('开始分析所有网页...', 'info');
    try {
        const data = await apiPost('/analysis/all');
        showToast(data.message, 'success');
        loadAnalysisData();
    } catch (err) {
        showToast('分析失败', 'error');
    }
});

async function loadAnalysisData() {
    try {
        const [stats, terms] = await Promise.all([
            apiGet('/analysis/statistics'),
            apiGet('/analysis/top-terms?limit=50')
        ]);

        // 渲染高频词语
        const termsDiv = document.getElementById('topTerms');
        if (terms && terms.length > 0) {
            const maxFreq = terms[0].totalFrequency;
            termsDiv.innerHTML = terms.map(t => {
                const size = Math.max(12, Math.min(24, 12 + (t.totalFrequency / maxFreq) * 12));
                return `<span class="tag" style="font-size:${size}px" title="文档频率: ${t.documentFrequency}">${escapeHtml(t.word)}<span class="freq">${t.totalFrequency}</span></span>`;
            }).join('');
        } else {
            termsDiv.innerHTML = '<p class="empty-text">请先分析网页</p>';
        }
    } catch (err) {
        console.error('加载分析数据失败', err);
    }
}

// ==================== 索引功能 ====================

document.getElementById('buildIndexBtn').addEventListener('click', async () => {
    showToast('开始构建倒排索引...', 'info');
    try {
        const data = await apiPost('/index/build');
        showToast(data.message, 'success');
        loadIndexData();
    } catch (err) {
        showToast('构建索引失败', 'error');
    }
});

document.getElementById('clearIndexBtn').addEventListener('click', async () => {
    if (!confirm('确定要清空所有索引吗？')) return;
    try {
        await apiPost('/index/clear');
        showToast('索引已清空', 'success');
        loadIndexData();
    } catch (err) {
        showToast('清空索引失败', 'error');
    }
});

document.getElementById('lookupBtn').addEventListener('click', async () => {
    const word = document.getElementById('termLookup').value.trim();
    if (!word) { showToast('请输入词语', 'error'); return; }

    try {
        const data = await apiGet(`/index/term/${encodeURIComponent(word)}`);
        renderIndexDetail(data);
    } catch (err) {
        showToast('查询失败', 'error');
    }
});

function renderIndexDetail(data) {
    const div = document.getElementById('indexDetail');
    document.getElementById('indexDetailCount').textContent = data.length || 0;

    if (!data || data.length === 0) {
        div.innerHTML = '<p class="empty-text">未找到该词语的索引记录</p>';
        return;
    }

    div.innerHTML = data.map(item => `
        <div class="data-item">
            <span>文档ID: ${item.pageId} | 词频: ${item.termFrequency}</span>
            <span class="status status-done">已索引</span>
        </div>`).join('');
}

async function loadIndexData() {
    try {
        const [stats, terms] = await Promise.all([
            apiGet('/index/statistics'),
            apiGet('/index/terms')
        ]);

        // 渲染词语偏移量表
        const termsDiv = document.getElementById('termOffsetList');
        document.getElementById('termOffsetCount').textContent = terms ? terms.length : 0;

        if (terms && terms.length > 0) {
            let html = `<table class="data-table">
                <thead><tr><th>词语ID</th><th>词语</th><th>文档频率</th><th>总频率</th><th>索引记录数</th></tr></thead>
                <tbody>`;
            terms.forEach(t => {
                html += `<tr>
                    <td>${t.termId}</td>
                    <td>${escapeHtml(t.word)}</td>
                    <td>${t.documentFrequency}</td>
                    <td>${t.totalFrequency}</td>
                    <td>${t.indexRecords}</td>
                </tr>`;
            });
            html += '</tbody></table>';
            termsDiv.innerHTML = html;
        } else {
            termsDiv.innerHTML = '<p class="empty-text">请先构建索引</p>';
        }
    } catch (err) {
        console.error('加载索引数据失败', err);
    }
}

// ==================== 统计功能 ====================

async function loadStats() {
    try {
        const stats = await apiGet('/crawl/statistics');
        document.getElementById('statTotalPages').textContent = stats.totalPages || 0;
        document.getElementById('statCrawled').textContent = stats.crawledPages || 0;
        document.getElementById('statAnalyzed').textContent = stats.analyzedPages || 0;
        document.getElementById('statIndexed').textContent = stats.indexedPages || 0;
        document.getElementById('statTerms').textContent = stats.bloomFilterBits || 0;
        document.getElementById('statIndexRecords').textContent = stats.pendingUrls || 0;
    } catch (err) {
        console.error('加载统计数据失败', err);
    }
}

// ==================== 工具函数 ====================

function escapeHtml(text) {
    if (!text) return '';
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

// ==================== 初始化 ====================
document.addEventListener('DOMContentLoaded', () => {
    loadStats();
});
