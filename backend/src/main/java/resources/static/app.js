const state = {
  currentUser: "",
  currentRole: "user",
  loggedIn: false,
  premium: false,
  coins: 50,
  selectedModel: "gpt",
  soundOn: true,
  chat: []
};

const els = {
  authView: document.getElementById('authView'),
  appView: document.getElementById('appView'),
  adminSection: document.getElementById('admin'),
  adminNav: document.getElementById('adminNav'),
  soundToggle: document.getElementById('soundToggle'),
  toast: document.getElementById('toast'),
  console: document.getElementById('console'),
  bootConsole: document.getElementById('bootConsole'),
  coinCount: document.getElementById('coinCount'),
  premiumState: document.getElementById('premiumState'),
  roleState: document.getElementById('roleState'),
  modelState: document.getElementById('modelState'),
  chatLog: document.getElementById('chatLog'),
  prompt: document.getElementById('prompt'),
  quickMessage: document.getElementById('quickMessage'),
  aiMode: document.getElementById('aiMode'),
  pluginName: document.getElementById('pluginName'),
  pluginVersion: document.getElementById('pluginVersion'),
  languageMode: document.getElementById('languageMode'),
  outputStyle: document.getElementById('outputStyle'),
  buildMode: document.getElementById('buildMode'),
  premiumPlan: document.getElementById('premiumPlan'),
  errorBackdrop: document.getElementById('errorBackdrop'),
  errorText: document.getElementById('errorText')
};

function toast(msg){ els.toast.textContent = msg; els.toast.classList.add('show'); clearTimeout(window.__toastTimer); window.__toastTimer = setTimeout(()=>els.toast.classList.remove('show'), 1600); }
function beep(freq=660, duration=0.05){ if(!state.soundOn) return; try{ const ctx = new (window.AudioContext || window.webkitAudioContext)(); const o = ctx.createOscillator(); const g = ctx.createGain(); o.type = 'sine'; o.frequency.value = freq; g.gain.value = 0.03; o.connect(g); g.connect(ctx.destination); o.start(); o.stop(ctx.currentTime + duration); o.onended = () => ctx.close(); }catch(e){} }
function renderBoot(){ els.bootConsole.innerHTML = `[Easy Ai] Secure mode ready.
[Auth] Login or register required.
[Security] Admin panel hidden until admin login.
[Billing] Coin store connected.
[AI] Ready for prompt routing.<span class="caret"></span>`; }
function renderConsole(lines){ els.console.innerHTML = lines.join('\n') + '\n<span class="caret"></span>'; }
function addBubble(role, text){ const div = document.createElement('div'); div.className = `bubble ${role}`; div.textContent = text; els.chatLog.appendChild(div); els.chatLog.scrollTop = els.chatLog.scrollHeight; }
function renderChat(){ els.chatLog.innerHTML = ''; if(state.chat.length === 0){ addBubble('ai', 'Welcome. Type a prompt and I will build a structured plugin plan.'); } else { state.chat.forEach(m => addBubble(m.role, m.text)); } }
function updateUI(){ els.coinCount.textContent = state.coins; els.premiumState.textContent = state.premium ? 'Yes' : 'No'; els.roleState.textContent = state.currentRole === 'admin' ? 'Admin' : 'User'; els.modelState.textContent = state.selectedModel.toUpperCase(); document.querySelectorAll('.chip').forEach(ch => ch.classList.toggle('active', ch.dataset.model === state.selectedModel)); }
function showApp(){ els.authView.classList.add('hidden'); els.appView.style.display = 'block'; if(state.currentRole === 'admin'){ els.adminSection.classList.remove('hidden'); els.adminNav.classList.remove('hidden'); } else { els.adminSection.classList.add('hidden'); els.adminNav.classList.add('hidden'); } updateUI(); renderChat(); renderConsole([`[Session] User: ${state.currentUser || 'Guest'}`, `[Role] ${state.currentRole}`, `[Coins] ${state.coins}`, `[Premium] ${state.premium ? 'Active' : 'Inactive'}`, `[AI] ${state.selectedModel.toUpperCase()} selected`]);}
function api(path, method='POST', body){ return fetch(path, { method, headers: {'Content-Type': 'application/json'}, body: body ? JSON.stringify(body) : undefined, credentials: 'same-origin' }).then(r => r.json()); }
function authUser(user){ state.loggedIn = true; state.currentUser = user.username; state.currentRole = user.role; state.premium = !!user.premium; state.coins = user.coins || 50; updateUI(); showApp(); toast(`Logged in as ${user.username}`); beep(820); }

document.querySelectorAll('.tab').forEach(btn => btn.addEventListener('click', ()=>{ document.querySelectorAll('.tab').forEach(b => b.classList.remove('active')); btn.classList.add('active'); document.getElementById('loginFormBox').classList.toggle('hidden', btn.dataset.tab !== 'login'); document.getElementById('registerFormBox').classList.toggle('hidden', btn.dataset.tab !== 'register'); beep(620); }));
document.getElementById('guestBtn').addEventListener('click', ()=>document.querySelector('.tab[data-tab="register"]').click());
document.getElementById('haveAccountBtn').addEventListener('click', ()=>document.querySelector('.tab[data-tab="login"]').click());

document.getElementById('loginBtn').addEventListener('click', async ()=>{ const email = document.getElementById('loginEmail').value.trim(); const password = document.getElementById('loginPassword').value.trim(); if(!email || !password){ toast('Enter email and password'); return; } const res = await api('/api/auth/login', 'POST', {email, password}); if(res.ok) authUser(res.data); else toast(res.message); });
document.getElementById('registerBtn').addEventListener('click', async ()=>{ const username = document.getElementById('regUser').value.trim(); const email = document.getElementById('regEmail').value.trim(); const password = document.getElementById('regPassword').value.trim(); if(!username || !email || !password){ toast('Fill all fields'); return; } const res = await api('/api/auth/register', 'POST', {username, email, password}); if(res.ok) authUser(res.data); else toast(res.message); });
document.getElementById('googleLogin').addEventListener('click', ()=>toast('Google OAuth needs backend credentials.'));
document.getElementById('discordLogin').addEventListener('click', ()=>toast('Discord OAuth needs backend credentials.'));
document.getElementById('googleRegister').addEventListener('click', ()=>toast('Google OAuth needs backend credentials.'));
document.getElementById('discordRegister').addEventListener('click', ()=>toast('Discord OAuth needs backend credentials.'));

document.getElementById('sendBtn').addEventListener('click', async ()=>{ const prompt = (els.prompt.value || '').trim(); if(!prompt){ toast('Type a message first'); return; } addBubble('user', prompt); state.chat.push({role:'user', text: prompt}); const body = { message: prompt, pluginName: els.pluginName.value, version: els.pluginVersion.value, language: els.languageMode.value, model: state.selectedModel, style: els.outputStyle.value, buildMode: els.buildMode.value }; const res = await api('/api/chat', 'POST', body); if(res.ok){ addBubble('ai', res.data.reply); state.chat.push({role:'ai', text: res.data.reply}); state.coins = res.data.coins; updateUI(); renderConsole(['[AI] Prompt received.', `[AI] Model: ${state.selectedModel.toUpperCase()}`, '[Coins] 5 coins deducted.', '[Build] Plugin plan generated.']); toast('Answer generated'); beep(860); } else { openError(res.message); } els.prompt.value = ''; });
document.getElementById('quickSendBtn').addEventListener('click', ()=>{ els.prompt.value = els.quickMessage.value; document.getElementById('sendBtn').click(); els.quickMessage.value = ''; });
document.getElementById('fixErrorBtn').addEventListener('click', ()=>openError('Press Fix Error when a build problem appears.'));
document.getElementById('applyFixBtn').addEventListener('click', ()=>{ els.errorBackdrop.classList.remove('show'); addBubble('ai', 'Error fixed. Rebuilding with safe defaults and corrected output path.'); state.chat.push({role:'ai', text:'Error fixed. Rebuilding with safe defaults and corrected output path.'}); toast('Error fixed'); beep(980); });
document.getElementById('closeErrorBtn').addEventListener('click', ()=>els.errorBackdrop.classList.remove('show'));
document.getElementById('clearChatBtn').addEventListener('click', ()=>{ state.chat = []; renderChat(); toast('Chat cleared'); beep(560); });

document.getElementById('buyPremiumBtn').addEventListener('click', async ()=>{ const plan = Number(els.premiumPlan.value); if(plan <= 0){ toast('Select a premium plan'); return; } const res = await api('/api/premium/buy', 'POST', { plan }); if(res.ok){ state.premium = true; updateUI(); toast('Premium activated'); beep(920); addBubble('ai', `Premium activated for ${plan === 1 ? 'monthly' : 'yearly'} plan.`); state.chat.push({role:'ai', text:`Premium activated for ${plan === 1 ? 'monthly' : 'yearly'} plan.`}); } else toast(res.message); });
document.querySelectorAll('.buyBtn').forEach(btn => btn.addEventListener('click', async ()=>{ const res = await api('/api/coins/buy', 'POST', { coins: Number(btn.dataset.coins), price: Number(btn.dataset.price) }); if(res.ok){ state.coins = res.data.coins; updateUI(); toast(`${res.data.added} coins added`); beep(780); addBubble('ai', `Coin purchase confirmed: ${res.data.added} coins for ₹${res.data.price}.`); state.chat.push({role:'ai', text:`Coin purchase confirmed: ${res.data.added} coins for ₹${res.data.price}.`}); } else toast(res.message); }));
document.querySelectorAll('.chip').forEach(ch => ch.addEventListener('click', ()=>{ state.selectedModel = ch.dataset.model; updateUI(); toast(`Model selected: ${state.selectedModel.toUpperCase()}`); beep(700); }));
document.getElementById('soundToggle').addEventListener('click', ()=>{ state.soundOn = !state.soundOn; document.getElementById('soundToggle').textContent = `Sound: ${state.soundOn ? 'On' : 'Off'}`; toast(`Sound ${state.soundOn ? 'enabled' : 'disabled'}`); if(state.soundOn) beep(500); });
document.getElementById('openBuilderBtn').addEventListener('click', ()=>document.getElementById('builder').scrollIntoView({behavior:'smooth'}));
document.getElementById('openCoinsBtn').addEventListener('click', ()=>document.getElementById('coins').scrollIntoView({behavior:'smooth'}));
document.getElementById('openPremiumBtn').addEventListener('click', ()=>document.getElementById('premiumPlan').focus());

function openError(message){ els.errorText.textContent = message; els.errorBackdrop.classList.add('show'); addBubble('err', message); state.chat.push({role:'err', text:message}); beep(220, .08); }

document.querySelectorAll('.menu-btn').forEach(btn => btn.addEventListener('click', ()=>{ document.querySelectorAll('.menu-btn').forEach(b => b.classList.remove('active')); btn.classList.add('active'); const map = { dashboard:'<h3 style="margin-top:0">Dashboard</h3><p>Admins can control users, coins, payments, and AI routing here.</p>', users:'<h3 style="margin-top:0">Users</h3><p>User management tools.</p>', coins:'<h3 style="margin-top:0">Coins</h3><p>Pack settings: 50, 200, 10000.</p>', payments:'<h3 style="margin-top:0">Payments</h3><p>UPI, QR, Paytm, and PhonePe setup.</p>', ai:'<h3 style="margin-top:0">AI Routing</h3><p>GPT, Gemini, Claude, fallback order, and premium controls.</p>' }; document.getElementById('adminPanel').innerHTML = map[btn.dataset.admin]; beep(700); }));

renderBoot();
fetch('/api/auth/me', {credentials:'same-origin'}).then(r => r.json()).then(res => { if(res.ok) { authUser(res.data); } else { document.querySelector('.tab[data-tab="login"]').click(); }});
