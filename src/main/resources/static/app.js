"use strict";

/* ---------- utilidades ---------- */
const $ = (s, r = document) => r.querySelector(s);
const esc = v => String(v ?? "").replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const brl = v => Number(v || 0).toLocaleString("pt-BR", { style: "currency", currency: "BRL" });
const dh = v => v ? new Date(v).toLocaleString("pt-BR", { dateStyle: "short", timeStyle: "short" }) : "—";
const dt = v => v ? new Date(v + "T00:00:00").toLocaleDateString("pt-BR") : "—";
const hoje = () => new Date().toISOString().slice(0, 10);
const ROTULO = {
  AGENDADA: "Agendada", EM_ANDAMENTO: "Em andamento", CONCLUIDA: "Concluída", CANCELADA: "Cancelada",
  CARRO: "Carro", VAN: "Van", ONIBUS: "Ônibus", CAMINHAO: "Caminhão", ADMIN: "Administrador", USER: "Usuário"
};
const tag = v => `<span class="tag ${esc(v)}">${esc(ROTULO[v] || v)}</span>`;
const opts = (lista, sel) => lista.map(([v, t]) => `<option value="${esc(v)}"${String(v) === String(sel) ? " selected" : ""}>${esc(t)}</option>`).join("");
const enumOpts = (chaves, sel) => opts(chaves.map(k => [k, ROTULO[k]]), sel);

function toast(msg, erro) {
  const d = document.createElement("div");
  if (erro) d.className = "e";
  d.textContent = msg;
  $("#toast").append(d);
  setTimeout(() => d.remove(), 4500);
}

/* ---------- API ---------- */
const sessao = {
  get token() { return sessionStorage.getItem("token"); },
  get usuario() { try { return JSON.parse(sessionStorage.getItem("usuario")); } catch { return null; } },
  salvar(r) { sessionStorage.setItem("token", r.token); sessionStorage.setItem("usuario", JSON.stringify({ id: r.usuarioId, nome: r.nome, perfil: r.perfil })); },
  limpar() { sessionStorage.clear(); }
};
const ehAdmin = () => sessao.usuario?.perfil === "ADMIN";

class ErroApi extends Error {
  constructor(corpo, status) { super(corpo?.mensagem || "Erro inesperado."); this.status = status; this.detalhes = corpo?.detalhes || []; }
}

async function api(metodo, caminho, corpo, bruto) {
  const cab = { Accept: "application/json" };
  if (sessao.token) cab.Authorization = "Bearer " + sessao.token;
  if (corpo !== undefined) cab["Content-Type"] = "application/json";
  let r;
  try {
    r = await fetch("/v1" + caminho, { method: metodo, headers: cab, body: corpo !== undefined ? JSON.stringify(corpo) : undefined });
  } catch {
    throw new ErroApi({ mensagem: "Não foi possível conectar ao servidor." }, 0);
  }
  if (r.status === 401 && caminho !== "/autenticacao/entrar") { sair(true); throw new ErroApi({ mensagem: "Sessão expirada." }, 401); }
  if (!r.ok) {
    let j = null; try { j = await r.json(); } catch { /* sem corpo */ }
    throw new ErroApi(j, r.status);
  }
  if (bruto) return r;
  if (r.status === 204) return null;
  return r.json();
}

const qs = o => { const p = new URLSearchParams(); Object.entries(o).forEach(([k, v]) => { if (v !== "" && v != null) p.set(k, v); }); const s = p.toString(); return s ? "?" + s : ""; };

async function baixar(caminho) {
  try {
    const r = await api("GET", caminho, undefined, true);
    const blob = await r.blob();
    const m = /filename="?([^";]+)"?/.exec(r.headers.get("Content-Disposition") || "");
    const a = document.createElement("a");
    a.href = URL.createObjectURL(blob);
    a.download = m ? m[1] : "relatorio";
    document.body.append(a); a.click(); a.remove();
    setTimeout(() => URL.revokeObjectURL(a.href), 2000);
  } catch (e) { toast(e.message, true); }
}

/* ---------- modal / formulários ---------- */
function fechar() { $("#modal").innerHTML = ""; }

function formulario({ titulo, campos, valores = {}, salvar, rotuloSalvar = "Salvar" }) {
  const html = campos.map(c => {
    const v = valores[c.nome] ?? c.padrao ?? "";
    let ctl;
    if (c.tipo === "select") ctl = `<select name="${c.nome}" ${c.req ? "required" : ""}>${c.vazio ? '<option value="">Selecione…</option>' : ""}${c.opcoes(v)}</select>`;
    else if (c.tipo === "check") ctl = `<input type="checkbox" name="${c.nome}" ${v ? "checked" : ""} style="width:20px;height:20px">`;
    else ctl = `<input name="${c.nome}" type="${c.tipo || "text"}" value="${esc(v)}" ${c.req ? "required" : ""} ${c.max ? `maxlength="${c.max}"` : ""} ${c.min != null ? `min="${c.min}"` : ""} ${c.step ? `step="${c.step}"` : ""} ${c.auto ? `autocomplete="${c.auto}"` : ""}>`;
    return `<label>${esc(c.rotulo)}${ctl}</label>`;
  }).join("");
  $("#modal").innerHTML = `<div class="modal"><form novalidate><h2>${esc(titulo)}</h2>${html}<div id="fErro" class="err hidden"></div>
    <div class="actions"><button type="button" class="btn" data-x>Cancelar</button><button class="btn pri" type="submit">${esc(rotuloSalvar)}</button></div></form></div>`;
  const f = $("#modal form");
  f.querySelector("[data-x]").onclick = fechar;
  f.querySelector("input,select")?.focus();
  f.onsubmit = async ev => {
    ev.preventDefault();
    if (!f.reportValidity()) return;
    const dados = {};
    campos.forEach(c => {
      const el = f.elements[c.nome];
      let v = c.tipo === "check" ? el.checked : el.value;
      if (c.tipo === "number") v = v === "" ? null : Number(v);
      else if (c.tipo === "select" && c.num) v = v === "" ? null : Number(v);
      else if (v === "" && c.opcional) v = null;
      dados[c.nome] = v;
    });
    const btn = f.querySelector(".pri"); btn.disabled = true;
    try { await salvar(dados); fechar(); }
    catch (e) {
      const box = $("#fErro");
      box.innerHTML = esc(e.message) + (e.detalhes.length ? `<ul>${e.detalhes.map(d => `<li>${esc(d)}</li>`).join("")}</ul>` : "");
      box.classList.remove("hidden");
      btn.disabled = false;
    }
  };
}

function confirmar(texto, acao) {
  $("#modal").innerHTML = `<div class="modal"><div class="box"><h2>Confirmar</h2><p>${esc(texto)}</p>
    <div class="actions"><button class="btn" data-x>Cancelar</button><button class="btn bad" data-ok>Excluir</button></div></div></div>`;
  $("#modal [data-x]").onclick = fechar;
  $("#modal [data-ok]").onclick = async () => {
    try { await acao(); fechar(); } catch (e) { fechar(); toast(e.message + (e.detalhes[0] ? " " + e.detalhes[0] : ""), true); }
  };
}

/* ---------- tabela genérica ---------- */
function tabela(cols, linhas, acoes) {
  if (!linhas.length) return `<div class="empty">Nenhum registro encontrado.</div>`;
  return `<div class="tbl-wrap"><table><thead><tr>${cols.map(c => `<th>${esc(c.t)}</th>`).join("")}${acoes ? "<th></th>" : ""}</tr></thead><tbody>
    ${linhas.map(l => `<tr>${cols.map(c => `<td>${c.h(l)}</td>`).join("")}${acoes ? `<td class="act">${acoes(l)}</td>` : ""}</tr>`).join("")}</tbody></table></div>`;
}
const btnAcoes = l => ehAdmin() ? `<button class="btn sm" data-ed="${l.id}">Editar</button><button class="btn sm bad" data-ex="${l.id}">Excluir</button>` : "";

/* ---------- páginas ---------- */
const paginas = {};
let cache = {};

async function carregarRefs() {
  const [v, m] = await Promise.all([api("GET", "/veiculos"), api("GET", "/motoristas")]);
  cache.veiculos = v; cache.motoristas = m;
}

/* Painel */
paginas.painel = {
  titulo: "Painel",
  async render(el) {
    const p = await api("GET", "/painel");
    const meses = ["Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"];
    const max = Math.max(1, ...p.viagensPorMes);
    el.innerHTML = `
      <div class="grid kpis">
        <div class="card kpi n"><small>Veículos</small><strong>${p.totalVeiculos}</strong></div>
        <div class="card kpi n"><small>Motoristas</small><strong>${p.totalMotoristas}</strong></div>
        <div class="card kpi p"><small>Viagens</small><strong>${p.totalViagens}</strong></div>
        <div class="card kpi p"><small>Pendentes</small><strong>${p.viagensPendentes}</strong></div>
        <div class="card kpi o"><small>Concluídas</small><strong>${p.viagensConcluidas}</strong></div>
        <div class="card kpi o"><small>Faturamento (concluídas)</small><strong>${brl(p.faturamentoConcluido)}</strong></div>
      </div>
      <div class="grid two">
        <div class="card"><h3>Viagens por mês (${new Date().getFullYear()})</h3>
          <div class="chart">${p.viagensPorMes.map((n, i) => `<div>${n}<i style="height:${Math.round(n / max * 150)}px"></i>${meses[i]}</div>`).join("")}</div></div>
        <div class="card"><h3>Alertas de manutenção</h3>
          ${p.alertas.length ? p.alertas.map(a => `<div class="alerta ${a.vencida ? "v" : "a"}"><b>${esc(a.placa)}</b> · ${esc(a.modelo)}<br><span style="color:var(--mut)">${esc(a.motivo)}</span></div>`).join("") : `<div class="empty">Nenhum alerta.</div>`}
        </div>
      </div>`;
  }
};

/* Agenda */
paginas.agenda = {
  titulo: "Agenda",
  async render(el) {
    await carregarRefs();
    el.innerHTML = `<div class="card"><form class="bar" id="fAg">
      <label>Data<input type="date" name="data" value="${hoje()}"></label>
      <label>Motorista<select name="motoristaId"><option value="">Todos</option>${opts(cache.motoristas.map(m => [m.id, m.nome]))}</select></label>
      <label>Veículo<select name="veiculoId"><option value="">Todos</option>${opts(cache.veiculos.map(v => [v.id, v.placa]))}</select></label>
      <label>Status<select name="status"><option value="">Todos</option>${enumOpts(["AGENDADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"])}</select></label>
      <button class="btn pri">Filtrar</button></form><div id="agRes"></div></div>`;
    const buscar = async () => {
      const f = Object.fromEntries(new FormData($("#fAg")));
      try {
        const l = await api("GET", "/agenda" + qs(f));
        $("#agRes").innerHTML = tabela(colunasViagem(true), l);
      } catch (e) { toast(e.message, true); }
    };
    $("#fAg").onsubmit = ev => { ev.preventDefault(); buscar(); };
    buscar();
  }
};

const colunasViagem = (semId) => [
  ...(semId ? [] : [{ t: "#", h: v => v.id }]),
  { t: "Data/hora", h: v => dh(v.dataHora) },
  { t: "Origem", h: v => esc(v.origem) },
  { t: "Destino", h: v => esc(v.destino) },
  { t: "Veículo", h: v => esc(v.veiculoPlaca) },
  { t: "Motorista", h: v => esc(v.motoristaNome) },
  { t: "Valor", h: v => brl(v.valor) },
  { t: "Status", h: v => tag(v.status) }
];

/* Viagens */
const estViagens = { pagina: 0, status: "", inicio: "", fim: "" };
paginas.viagens = {
  titulo: "Viagens",
  acoes: () => ehAdmin() ? `<button class="btn" data-exp="csv">CSV</button> <button class="btn" data-exp="pdf">PDF</button> <button class="btn pri" id="novo">+ Nova viagem</button>` : `<button class="btn" data-exp="csv">CSV</button> <button class="btn" data-exp="pdf">PDF</button>`,
  async render(el) {
    await carregarRefs();
    el.innerHTML = `<div class="card"><form class="bar" id="fV">
      <label>Status<select name="status"><option value="">Todos</option>${enumOpts(["AGENDADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"], estViagens.status)}</select></label>
      <label>De<input type="date" name="inicio" value="${estViagens.inicio}"></label>
      <label>Até<input type="date" name="fim" value="${estViagens.fim}"></label>
      <button class="btn pri">Filtrar</button></form><div id="vRes"></div></div>`;
    const carregar = async () => {
      try {
        const p = await api("GET", "/viagens" + qs({ ...estViagens, tamanho: 10 }));
        $("#vRes").innerHTML = tabela(colunasViagem(false), p.conteudo, btnAcoes) +
          `<div class="pager"><button class="btn sm" data-pg="-1" ${p.pagina <= 0 ? "disabled" : ""}>←</button>
           <span>Página ${p.pagina + 1} de ${Math.max(1, p.totalPaginas)} · ${p.totalElementos} registro(s)</span>
           <button class="btn sm" data-pg="1" ${p.pagina + 1 >= p.totalPaginas ? "disabled" : ""}>→</button></div>`;
        $("#vRes").onclick = ev => {
          const b = ev.target.closest("button"); if (!b) return;
          if (b.dataset.pg) { estViagens.pagina += Number(b.dataset.pg); carregar(); }
          if (b.dataset.ed) formViagem(p.conteudo.find(x => x.id == b.dataset.ed), carregar);
          if (b.dataset.ex) confirmar("Excluir esta viagem?", async () => { await api("DELETE", "/viagens/" + b.dataset.ex); toast("Viagem excluída."); carregar(); });
        };
      } catch (e) { toast(e.message, true); }
    };
    $("#fV").onsubmit = ev => { ev.preventDefault(); Object.assign(estViagens, Object.fromEntries(new FormData(ev.target)), { pagina: 0 }); carregar(); };
    carregar();
    el._recarregar = carregar;
  },
  ligar(el) {
    $("#novo")?.addEventListener("click", () => formViagem(null, el._recarregar));
    document.querySelectorAll("[data-exp]").forEach(b => b.onclick = () => baixar("/viagens/exportar" + qs({ formato: b.dataset.exp, status: estViagens.status, inicio: estViagens.inicio, fim: estViagens.fim })));
  }
};

function formViagem(v, depois) {
  formulario({
    titulo: v ? "Editar viagem" : "Nova viagem",
    valores: v ? { ...v, dataHora: (v.dataHora || "").slice(0, 16) } : {},
    campos: [
      { nome: "origem", rotulo: "Origem", req: 1, max: 255 },
      { nome: "destino", rotulo: "Destino", req: 1, max: 255 },
      { nome: "dataHora", rotulo: "Data e hora", tipo: "datetime-local", req: 1 },
      { nome: "valor", rotulo: "Valor (R$)", tipo: "number", req: 1, min: 0, step: "0.01" },
      { nome: "status", rotulo: "Status", tipo: "select", req: 1, padrao: "AGENDADA", opcoes: s => enumOpts(["AGENDADA", "EM_ANDAMENTO", "CONCLUIDA", "CANCELADA"], s) },
      { nome: "veiculoId", rotulo: "Veículo", tipo: "select", num: 1, req: 1, vazio: 1, opcoes: s => opts(cache.veiculos.filter(x => x.ativo || x.id == s).map(x => [x.id, `${x.placa} · ${x.modelo}`]), s) },
      { nome: "motoristaId", rotulo: "Motorista", tipo: "select", num: 1, req: 1, vazio: 1, opcoes: s => opts(cache.motoristas.map(x => [x.id, x.nome]), s) }
    ],
    salvar: async d => { await api(v ? "PUT" : "POST", "/viagens" + (v ? "/" + v.id : ""), d); toast(v ? "Viagem atualizada." : "Viagem agendada."); depois?.(); }
  });
}

/* Veículos */
paginas.veiculos = {
  titulo: "Veículos",
  acoes: () => ehAdmin() ? `<button class="btn pri" id="novo">+ Novo veículo</button>` : "",
  async render(el) {
    const carregar = async () => {
      const l = await api("GET", "/veiculos");
      el.innerHTML = `<div class="card">${tabela([
        { t: "Placa", h: v => `<b>${esc(v.placa)}</b>` }, { t: "Modelo", h: v => esc(v.modelo) },
        { t: "Tipo", h: v => esc(ROTULO[v.tipo] || v.tipo) }, { t: "Capacidade", h: v => v.capacidade },
        { t: "Km atual", h: v => Number(v.quilometragemAtual || 0).toLocaleString("pt-BR") },
        { t: "Situação", h: v => v.ativo ? `<span class="tag ok">Ativo</span>` : `<span class="tag off">Inativo</span>` }
      ], l, btnAcoes)}</div>`;
      el.onclick = ev => {
        const b = ev.target.closest("button"); if (!b) return;
        if (b.dataset.ed) formVeiculo(l.find(x => x.id == b.dataset.ed), carregar);
        if (b.dataset.ex) confirmar("Excluir este veículo?", async () => { await api("DELETE", "/veiculos/" + b.dataset.ex); toast("Veículo excluído."); carregar(); });
      };
    };
    el._recarregar = carregar;
    await carregar();
  },
  ligar(el) { $("#novo")?.addEventListener("click", () => formVeiculo(null, el._recarregar)); }
};

function formVeiculo(v, depois) {
  formulario({
    titulo: v ? "Editar veículo" : "Novo veículo", valores: v || {},
    campos: [
      { nome: "placa", rotulo: "Placa", req: 1, max: 10 },
      { nome: "modelo", rotulo: "Modelo", req: 1, max: 255 },
      { nome: "capacidade", rotulo: "Capacidade (lugares)", tipo: "number", req: 1, min: 1 },
      { nome: "tipo", rotulo: "Tipo", tipo: "select", req: 1, padrao: "CARRO", opcoes: s => enumOpts(["CARRO", "VAN", "ONIBUS", "CAMINHAO"], s) },
      { nome: "quilometragemAtual", rotulo: "Quilometragem atual", tipo: "number", min: 0, padrao: 0 },
      { nome: "ativo", rotulo: "Ativo", tipo: "check", padrao: true }
    ],
    salvar: async d => { d.quilometragemAtual ??= 0; await api(v ? "PUT" : "POST", "/veiculos" + (v ? "/" + v.id : ""), d); toast("Veículo salvo."); depois?.(); }
  });
}

/* Motoristas */
paginas.motoristas = {
  titulo: "Motoristas",
  acoes: () => ehAdmin() ? `<button class="btn pri" id="novo">+ Novo motorista</button>` : "",
  async render(el) {
    const carregar = async () => {
      const l = await api("GET", "/motoristas");
      el.innerHTML = `<div class="card">${tabela([
        { t: "Nome", h: m => `<b>${esc(m.nome)}</b>` }, { t: "CNH", h: m => esc(m.cnh) }, { t: "Telefone", h: m => esc(m.telefone || "—") }
      ], l, btnAcoes)}</div>`;
      el.onclick = ev => {
        const b = ev.target.closest("button"); if (!b) return;
        if (b.dataset.ed) formMotorista(l.find(x => x.id == b.dataset.ed), carregar);
        if (b.dataset.ex) confirmar("Excluir este motorista?", async () => { await api("DELETE", "/motoristas/" + b.dataset.ex); toast("Motorista excluído."); carregar(); });
      };
    };
    el._recarregar = carregar;
    await carregar();
  },
  ligar(el) { $("#novo")?.addEventListener("click", () => formMotorista(null, el._recarregar)); }
};

function formMotorista(m, depois) {
  formulario({
    titulo: m ? "Editar motorista" : "Novo motorista", valores: m || {},
    campos: [
      { nome: "nome", rotulo: "Nome", req: 1, max: 255 },
      { nome: "cnh", rotulo: "CNH", req: 1, max: 20 },
      { nome: "telefone", rotulo: "Telefone", max: 255, opcional: 1, tipo: "tel" }
    ],
    salvar: async d => { await api(m ? "PUT" : "POST", "/motoristas" + (m ? "/" + m.id : ""), d); toast("Motorista salvo."); depois?.(); }
  });
}

/* Manutenções (admin) */
const estMan = { pagina: 0, situacao: "", inicio: "", fim: "" };
paginas.manutencoes = {
  titulo: "Manutenções", admin: true,
  acoes: () => `<button class="btn" data-exp="csv">CSV</button> <button class="btn" data-exp="pdf">PDF</button> <button class="btn pri" id="novo">+ Registrar manutenção</button>`,
  async render(el) {
    await carregarRefs();
    el.innerHTML = `<div class="card"><form class="bar" id="fM">
      <label>Situação<select name="situacao"><option value="">Todas</option>${enumOpts(["AGENDADA", "EM_ANDAMENTO", "CONCLUIDA"], estMan.situacao)}</select></label>
      <label>De<input type="date" name="inicio" value="${estMan.inicio}"></label>
      <label>Até<input type="date" name="fim" value="${estMan.fim}"></label>
      <button class="btn pri">Filtrar</button></form><div id="mRes"></div></div>`;
    const carregar = async () => {
      try {
        const p = await api("GET", "/manutencoes" + qs({ ...estMan, tamanho: 10 }));
        $("#mRes").innerHTML = tabela([
          { t: "Veículo", h: m => `<b>${esc(m.veiculoPlaca)}</b>` }, { t: "Descrição", h: m => esc(m.descricao) },
          { t: "Data", h: m => dt(m.dataManutencao) }, { t: "Km", h: m => Number(m.quilometragem).toLocaleString("pt-BR") },
          { t: "Próx. revisão", h: m => dt(m.proximaRevisao) + (m.proximaRevisaoKm ? ` · ${Number(m.proximaRevisaoKm).toLocaleString("pt-BR")} km` : "") },
          { t: "Situação", h: m => tag(m.situacao) }
        ], p.conteudo) +
          `<div class="pager"><button class="btn sm" data-pg="-1" ${p.pagina <= 0 ? "disabled" : ""}>←</button>
           <span>Página ${p.pagina + 1} de ${Math.max(1, p.totalPaginas)} · ${p.totalElementos} registro(s)</span>
           <button class="btn sm" data-pg="1" ${p.pagina + 1 >= p.totalPaginas ? "disabled" : ""}>→</button></div>`;
        $("#mRes").onclick = ev => { const b = ev.target.closest("[data-pg]"); if (b) { estMan.pagina += Number(b.dataset.pg); carregar(); } };
      } catch (e) { toast(e.message, true); }
    };
    $("#fM").onsubmit = ev => { ev.preventDefault(); Object.assign(estMan, Object.fromEntries(new FormData(ev.target)), { pagina: 0 }); carregar(); };
    el._recarregar = carregar;
    carregar();
  },
  ligar(el) {
    $("#novo")?.addEventListener("click", () => formulario({
      titulo: "Registrar manutenção",
      campos: [
        { nome: "veiculoId", rotulo: "Veículo", tipo: "select", num: 1, req: 1, vazio: 1, opcoes: s => opts(cache.veiculos.map(x => [x.id, `${x.placa} · ${x.modelo}`]), s) },
        { nome: "descricao", rotulo: "Descrição", req: 1, max: 255 },
        { nome: "dataManutencao", rotulo: "Data da manutenção", tipo: "date", req: 1, padrao: hoje() },
        { nome: "quilometragem", rotulo: "Quilometragem", tipo: "number", req: 1, min: 0 },
        { nome: "proximaRevisao", rotulo: "Próxima revisão (data)", tipo: "date", opcional: 1 },
        { nome: "proximaRevisaoKm", rotulo: "Próxima revisão (km)", tipo: "number", min: 0 },
        { nome: "situacao", rotulo: "Situação", tipo: "select", req: 1, padrao: "AGENDADA", opcoes: s => enumOpts(["AGENDADA", "EM_ANDAMENTO", "CONCLUIDA"], s) }
      ],
      salvar: async d => { await api("POST", "/manutencoes", d); toast("Manutenção registrada."); el._recarregar?.(); }
    }));
    document.querySelectorAll("[data-exp]").forEach(b => b.onclick = () => baixar("/manutencoes/exportar" + qs({ formato: b.dataset.exp, situacao: estMan.situacao, inicio: estMan.inicio, fim: estMan.fim })));
  }
};

/* Usuários (admin) */
paginas.usuarios = {
  titulo: "Usuários", admin: true,
  acoes: () => `<button class="btn pri" id="novo">+ Nova conta</button>`,
  async render(el) {
    const carregar = async () => {
      const l = await api("GET", "/usuarios");
      el.innerHTML = `<div class="card">${tabela([
        { t: "Nome", h: u => `<b>${esc(u.nome)}</b>` }, { t: "E-mail", h: u => esc(u.email) }, { t: "Perfil", h: u => tag(u.perfil) }
      ], l, u => u.id === sessao.usuario.id ? "" : `<button class="btn sm bad" data-ex="${u.id}">Excluir</button>`)}</div>`;
      el.onclick = ev => {
        const b = ev.target.closest("[data-ex]"); if (!b) return;
        confirmar("Excluir esta conta? O acesso é removido imediatamente.", async () => { await api("DELETE", "/usuarios/" + b.dataset.ex); toast("Conta excluída."); carregar(); });
      };
    };
    el._recarregar = carregar;
    await carregar();
  },
  ligar(el) {
    $("#novo")?.addEventListener("click", () => formulario({
      titulo: "Nova conta",
      campos: [
        { nome: "nome", rotulo: "Nome", req: 1, max: 150 },
        { nome: "email", rotulo: "E-mail", tipo: "email", req: 1, max: 255 },
        { nome: "senha", rotulo: "Senha (10+ caracteres, maiúscula, minúscula, número e símbolo)", tipo: "password", req: 1, max: 72, auto: "new-password" },
        { nome: "perfil", rotulo: "Perfil", tipo: "select", req: 1, padrao: "USER", opcoes: s => enumOpts(["USER", "ADMIN"], s) }
      ],
      salvar: async d => { await api("POST", "/usuarios", d); toast("Conta criada."); el._recarregar?.(); }
    }));
  }
};

/* Minha conta */
paginas.conta = {
  titulo: "Minha conta",
  async render(el) {
    const u = await api("GET", "/minha-conta");
    el.innerHTML = `<div class="card" style="max-width:520px"><form id="fC" class="grid" novalidate>
      <label>Nome<input name="nome" value="${esc(u.nome)}" required maxlength="150"></label>
      <label>E-mail<input name="email" type="email" value="${esc(u.email)}" required maxlength="255" autocomplete="username"></label>
      <label>Senha atual (obrigatória para salvar)<input name="senhaAtual" type="password" required maxlength="72" autocomplete="current-password"></label>
      <div class="row2">
        <label>Nova senha<input name="novaSenha" type="password" maxlength="72" autocomplete="new-password"></label>
        <label>Confirmar nova senha<input name="confirmarNovaSenha" type="password" maxlength="72" autocomplete="new-password"></label>
      </div>
      <div id="cErro" class="err hidden"></div>
      <div><button class="btn pri">Salvar alterações</button></div></form></div>`;
    $("#fC").onsubmit = async ev => {
      ev.preventDefault();
      if (!ev.target.reportValidity()) return;
      const d = Object.fromEntries(new FormData(ev.target));
      if (!d.novaSenha) { delete d.novaSenha; delete d.confirmarNovaSenha; }
      try {
        const r = await api("PUT", "/minha-conta", d);
        const s = sessao.usuario; s.nome = r.nome; sessionStorage.setItem("usuario", JSON.stringify(s));
        $("#uNome").textContent = r.nome;
        ev.target.senhaAtual.value = ev.target.novaSenha.value = ev.target.confirmarNovaSenha.value = "";
        $("#cErro").classList.add("hidden");
        toast("Conta atualizada.");
      } catch (e) {
        const box = $("#cErro");
        box.innerHTML = esc(e.message) + (e.detalhes.length ? `<ul>${e.detalhes.map(x => `<li>${esc(x)}</li>`).join("")}</ul>` : "");
        box.classList.remove("hidden");
      }
    };
  }
};

/* ---------- roteamento ---------- */
const MENU = [
  ["painel", "Painel"], ["agenda", "Agenda"], ["viagens", "Viagens"], ["veiculos", "Veículos"],
  ["motoristas", "Motoristas"], ["manutencoes", "Manutenções"], ["usuarios", "Usuários"], ["conta", "Minha conta"]
];

function montarMenu() {
  $("#nav").innerHTML = MENU.filter(([k]) => !paginas[k].admin || ehAdmin())
    .map(([k, t]) => `<a href="#${k}" data-k="${k}">${esc(t)}</a>`).join("");
}

async function rotear() {
  if (!sessao.token) return;
  let k = location.hash.slice(1);
  if (!paginas[k] || (paginas[k].admin && !ehAdmin())) k = "painel";
  const pg = paginas[k];
  document.querySelectorAll("#nav a").forEach(a => a.classList.toggle("on", a.dataset.k === k));
  $("#side").classList.remove("open");
  $("#titulo").textContent = pg.titulo;
  $("#acoes").innerHTML = pg.acoes ? pg.acoes() : "";
  const el = $("#view");
  el.onclick = null; el._recarregar = null;
  el.innerHTML = `<div class="empty">Carregando…</div>`;
  try {
    await pg.render(el);
    pg.ligar?.(el);
  } catch (e) {
    if (e.status !== 401) el.innerHTML = `<div class="card err">${esc(e.message)}</div>`;
  }
}

/* ---------- sessão ---------- */
function mostrarApp() {
  $("#login").classList.add("hidden");
  $("#app").classList.remove("hidden");
  const u = sessao.usuario;
  $("#uNome").textContent = u.nome;
  $("#uPerfil").textContent = ROTULO[u.perfil] || u.perfil;
  montarMenu();
  rotear();
}

function sair(expirou) {
  sessao.limpar();
  fechar();
  $("#app").classList.add("hidden");
  $("#login").classList.remove("hidden");
  if (expirou) toast("Sessão expirada. Entre novamente.", true);
}

$("#formLogin").onsubmit = async ev => {
  ev.preventDefault();
  const f = ev.target, box = $("#loginErro"), btn = f.querySelector("button");
  box.classList.add("hidden"); btn.disabled = true;
  try {
    const r = await api("POST", "/autenticacao/entrar", { email: f.email.value.trim(), senha: f.senha.value });
    sessao.salvar(r);
    f.senha.value = "";
    if (!location.hash) location.hash = "painel";
    mostrarApp();
  } catch (e) {
    box.textContent = e.message;
    box.classList.remove("hidden");
  } finally { btn.disabled = false; }
};
$("#btnSair").onclick = () => sair(false);
$("#btnMenu").onclick = () => $("#side").classList.toggle("open");
window.addEventListener("hashchange", rotear);

if (sessao.token && sessao.usuario) mostrarApp(); else $("#login").classList.remove("hidden");
