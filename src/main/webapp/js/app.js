const state = {
    user: null,
    fanfics: [],
    currentPage: 1,
    fanficsPerPage: 5,
    adminFanfics: [],
    adminUsersData: [],
    selectedAdminUserId: null
};

const formFanfic = document.getElementById('formFanfic');
const manualForm = document.getElementById('manualForm');
const toggleManualImport = document.getElementById('toggleManualImport');
const urlInput = document.getElementById('url');
const finishedDateInput = document.getElementById('finishedDate');
const userStarsInput = document.getElementById('userStars');
const estado = document.getElementById('estado');
const listaFanfics = document.getElementById('listaFanfics');
const paginacionFanfics = document.getElementById('paginacionFanfics');
const stats = document.getElementById('stats');
const contadorFanfics = document.getElementById('contadorFanfics');
const adminSection = document.getElementById('adminSection');
const adminUsers = document.getElementById('adminUsers');
const adminFanfics = document.getElementById('adminFanfics');
const adminFanficsTitle = document.getElementById('adminFanficsTitle');
const adminSelectedActions = document.getElementById('adminSelectedActions');
const adminEstado = document.getElementById('adminEstado');
const authStatus = document.getElementById('authStatus');
const authSection = document.getElementById('authSection');
const appSection = document.getElementById('appSection');
const authEstado = document.getElementById('authEstado');
const importSection = document.getElementById('importar');
const bibliotecaSection = document.getElementById('biblioteca');
const estadisticasSection = document.getElementById('estadisticas');
const registerForm = document.getElementById('registerForm');
const loginForm = document.getElementById('loginForm');
const registerCard = document.getElementById('registerCard');
const loginCard = document.getElementById('loginCard');
const showRegisterButton = document.getElementById('showRegisterButton');
const showLoginButton = document.getElementById('showLoginButton');

registerForm.addEventListener('submit', manejarRegistro);
loginForm.addEventListener('submit', manejarLogin);
formFanfic.addEventListener('submit', guardarFanfic);
manualForm.addEventListener('submit', guardarFanficManual);
toggleManualImport.addEventListener('click', alternarModoManual);
listaFanfics.addEventListener('click', manejarClicksBiblioteca);
listaFanfics.addEventListener('submit', manejarEdicionFanfic);
paginacionFanfics.addEventListener('click', manejarPaginacionBiblioteca);
adminUsers.addEventListener('click', manejarClickUsuariosAdmin);
adminFanfics.addEventListener('click', manejarClicksAdmin);
authStatus.addEventListener('click', manejarAccionesSesion);
showRegisterButton.addEventListener('click', () => cambiarVistaAuth('register'));
showLoginButton.addEventListener('click', () => cambiarVistaAuth('login'));

function init() {
    finishedDateInput.valueAsDate = new Date();
    userStarsInput.value = '5';
    cambiarVistaAuth('register');
    return cargarSesion();
}

function cargarSesion() {
    return solicitar('/api/auth/session')
        .then(({ data }) => {
            actualizarSesion(data.loggedIn ? data.user : null);
            if (data.loggedIn) {
                return cargarBibliotecaCompleta();
            }
            return null;
        })
        .catch(error => {
            actualizarSesion(null);
            authEstado.textContent = error.message;
        });
}

function manejarRegistro(evento) {
    evento.preventDefault();
    authEstado.textContent = 'Creando cuenta...';

    return solicitar('/api/auth/register', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            username: document.getElementById('registerUsername').value.trim(),
            email: document.getElementById('registerEmail').value.trim(),
            password: document.getElementById('registerPassword').value
        })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo crear la cuenta');
            }

            registerForm.reset();
            authEstado.textContent = 'Cuenta creada. Ya puedes usar tu biblioteca.';
            actualizarSesion(data.user);
            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            authEstado.textContent = error.message;
        });
}

function manejarLogin(evento) {
    evento.preventDefault();
    authEstado.textContent = 'Entrando...';

    return solicitar('/api/auth/login', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            identifier: document.getElementById('loginIdentifier').value.trim(),
            password: document.getElementById('loginPassword').value
        })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo iniciar sesion');
            }

            loginForm.reset();
            authEstado.textContent = 'Sesion iniciada.';
            actualizarSesion(data.user);
            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            authEstado.textContent = error.message;
        });
}

function manejarAccionesSesion(evento) {
    const openRegisterButton = evento.target.closest('[data-action="open-auth-register"]');
    if (openRegisterButton) {
        abrirCuenta('register');
        return;
    }

    const openLoginButton = evento.target.closest('[data-action="open-auth-login"]');
    if (openLoginButton) {
        abrirCuenta('login');
        return;
    }

    const boton = evento.target.closest('[data-action="logout"]');
    if (!boton) {
        return;
    }

    return solicitar('/api/auth/logout', { method: 'POST' })
        .finally(() => {
            actualizarSesion(null);
            estado.textContent = '';
            authEstado.textContent = 'Sesion cerrada.';
        });
}

function abrirCuenta(view) {
    cambiarVistaAuth(view);
    authSection.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function guardarFanfic(evento) {
    evento.preventDefault();
    estado.textContent = 'Importando datos desde AO3...';

    return solicitar('/api/fanfics/importar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            url: urlInput.value.trim(),
            finishedDate: finishedDateInput.value,
            userStars: Number(userStarsInput.value)
        })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo guardar el fanfic');
            }

            estado.textContent = `Fanfic guardado: ${data.fanfic.titulo}`;
            formFanfic.reset();
            finishedDateInput.valueAsDate = new Date();
            userStarsInput.value = '5';

            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            estado.textContent = error.message;
            mostrarModoManual();
        });
}

function guardarFanficManual(evento) {
    evento.preventDefault();
    estado.textContent = 'Guardando fanfic manualmente...';

    return solicitar('/api/fanfics/manual', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({
            url: urlInput.value.trim(),
            finishedDate: finishedDateInput.value,
            userStars: Number(userStarsInput.value),
            titulo: document.getElementById('manualTitulo').value.trim(),
            autor: document.getElementById('manualAutor').value.trim(),
            ao3Rating: document.getElementById('manualAo3Rating').value.trim(),
            wordCount: Number(document.getElementById('manualWordCount').value || 0),
            warnings: document.getElementById('manualWarnings').value.trim(),
            relationships: document.getElementById('manualRelationships').value.trim(),
            fandoms: document.getElementById('manualFandoms').value.trim(),
            categories: document.getElementById('manualCategories').value.trim()
        })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo guardar el fanfic manualmente');
            }

            estado.textContent = `Fanfic guardado: ${data.fanfic.titulo}`;
            formFanfic.reset();
            manualForm.reset();
            manualForm.classList.add('hidden');
            toggleManualImport.textContent = 'Usar modo manual';
            toggleManualImport.setAttribute('aria-expanded', 'false');
            finishedDateInput.valueAsDate = new Date();
            userStarsInput.value = '5';
            document.getElementById('manualWordCount').value = '0';

            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            estado.textContent = error.message;
        });
}

function cargarBibliotecaCompleta() {
    if (state.user?.isAdmin) {
        return cargarPanelAdmin();
    }

    return Promise.all([cargarFanfics(), cargarEstadisticas()]);
}

function cargarFanfics() {
    listaFanfics.innerHTML = '<div class="mensaje neutro">Cargando fanfics...</div>';
    paginacionFanfics.innerHTML = '';

    return solicitar('/api/fanfics')
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudieron cargar los fanfics');
            }

            contadorFanfics.textContent = `${data.fanfics.length} fanfic(s) guardado(s)`;
            state.fanfics = data.fanfics;

            if (data.fanfics.length === 0) {
                state.currentPage = 1;
                listaFanfics.innerHTML = `
                    <article class="empty-card">
                        <p class="empty-card__eyebrow">Biblioteca vacia</p>
                        <h3>Todavia no has guardado ningun fanfic</h3>
                        <p>
                            Empieza importando un enlace de AO3 para crear tu primera ficha. Cada entrada se guardara
                            solo en tu cuenta y luego podras editarla o borrarla.
                        </p>
                    </article>
                `;
                paginacionFanfics.innerHTML = '';
                return;
            }

            const totalPages = Math.ceil(state.fanfics.length / state.fanficsPerPage);
            state.currentPage = Math.min(state.currentPage, totalPages);
            renderPaginaBiblioteca();
        })
        .catch(error => {
            listaFanfics.innerHTML = `<div class="mensaje error">${escapeHtml(error.message)}</div>`;
            paginacionFanfics.innerHTML = '';
        });
}

function cargarEstadisticas() {
    stats.innerHTML = '<div class="mensaje neutro">Cargando estadisticas...</div>';

    return solicitar('/api/estadisticas')
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudieron cargar las estadisticas');
            }

            if (!data.enabled) {
                stats.innerHTML = `
                    <article class="empty-card empty-card--stats">
                        <p class="empty-card__eyebrow">Stats locked</p>
                        <h3>${data.totalFanfics}/10 fanfics guardados</h3>
                        <p>${escapeHtml(data.mensaje)}</p>
                        <div class="progress">
                            <span class="progress__bar" style="width: ${Math.min((data.totalFanfics / 10) * 100, 100)}%"></span>
                        </div>
                    </article>
                `;
                return;
            }

            stats.innerHTML = renderStats(data);
        })
        .catch(error => {
            stats.innerHTML = `<div class="mensaje error">${escapeHtml(error.message)}</div>`;
        });
}

function manejarClicksBiblioteca(evento) {
    const toggleButton = evento.target.closest('[data-action="toggle-details"]');
    if (toggleButton) {
        const card = toggleButton.closest('.fanfic-card');
        const expandida = card.classList.toggle('expanded');
        toggleButton.textContent = expandida ? '−' : '+';
        toggleButton.setAttribute('aria-expanded', String(expandida));
        toggleButton.setAttribute('aria-label', expandida ? 'Ocultar detalles' : 'Ver detalles');
        toggleButton.setAttribute('title', expandida ? 'Ocultar detalles' : 'Ver detalles');
        return;
    }

    const deleteButton = evento.target.closest('[data-action="delete-fanfic"]');
    if (deleteButton) {
        borrarFanfic(Number(deleteButton.dataset.fanficId));
    }
}

function manejarPaginacionBiblioteca(evento) {
    const pageButton = evento.target.closest('[data-action="go-page"]');
    if (!pageButton) {
        return;
    }

    state.currentPage = Number(pageButton.dataset.page);
    renderPaginaBiblioteca();
}

function alternarModoManual() {
    const visible = !manualForm.classList.contains('hidden');
    manualForm.classList.toggle('hidden', visible);
    toggleManualImport.textContent = visible ? 'Usar modo manual' : 'Ocultar modo manual';
    toggleManualImport.setAttribute('aria-expanded', String(!visible));
}

function mostrarModoManual() {
    manualForm.classList.remove('hidden');
    toggleManualImport.textContent = 'Ocultar modo manual';
    toggleManualImport.setAttribute('aria-expanded', 'true');
}

function manejarClicksAdmin(evento) {
    const deleteUserButton = evento.target.closest('[data-action="admin-delete-user"]');
    if (deleteUserButton) {
        borrarUsuarioComoAdmin(Number(deleteUserButton.dataset.userId));
        return;
    }

    const deleteButton = evento.target.closest('[data-action="admin-delete-fanfic"]');
    if (!deleteButton) {
        return;
    }

    borrarFanficComoAdmin(Number(deleteButton.dataset.fanficId));
}

function manejarClickUsuariosAdmin(evento) {
    const userButton = evento.target.closest('[data-action="select-admin-user"]');
    if (!userButton) {
        return;
    }

    state.selectedAdminUserId = Number(userButton.dataset.userId);
    renderAdminUsers(state.adminUsersData || []);
    renderAdminFanficsFiltrados();
}

function manejarEdicionFanfic(evento) {
    const form = evento.target.closest('.edit-form');
    if (!form) {
        return;
    }

    evento.preventDefault();

    const fanficId = Number(form.dataset.fanficId);
    const finishedDate = form.querySelector('[name="finishedDate"]').value;
    const userStars = Number(form.querySelector('[name="userStars"]').value);
    const feedback = form.querySelector('.edit-form__feedback');
    feedback.textContent = 'Guardando cambios...';

    return solicitar('/api/fanfics/actualizar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ fanficId, finishedDate, userStars })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo actualizar');
            }

            feedback.textContent = 'Cambios guardados.';
            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            feedback.textContent = error.message;
        });
}

function borrarFanfic(fanficId) {
    if (!confirm('¿Seguro que quieres borrar esta entrada de tu biblioteca?')) {
        return;
    }

    return solicitar('/api/fanfics/eliminar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ fanficId })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo borrar la entrada');
            }

            return cargarBibliotecaCompleta();
        })
        .catch(error => {
            estado.textContent = error.message;
        });
}

function cargarPanelAdmin() {
    adminSection.classList.remove('hidden');
    adminEstado.textContent = '';
    adminUsers.innerHTML = '<div class="mensaje neutro">Cargando usuarios...</div>';
    adminFanfics.innerHTML = '<div class="mensaje neutro">Selecciona un usuario para ver sus entradas.</div>';
    adminFanficsTitle.textContent = 'Entradas del usuario';
    adminSelectedActions.innerHTML = '';

    return Promise.all([
        solicitar('/api/admin/users'),
        solicitar('/api/admin/fanfics')
    ])
        .then(([usersResult, fanficsResult]) => {
            if (!usersResult.response.ok || !usersResult.data.ok) {
                throw new Error(usersResult.data.detalle || usersResult.data.mensaje || 'No se pudieron cargar los usuarios');
            }

            if (!fanficsResult.response.ok || !fanficsResult.data.ok) {
                throw new Error(fanficsResult.data.detalle || fanficsResult.data.mensaje || 'No se pudieron cargar las entradas');
            }

            state.adminUsersData = usersResult.data.users;
            state.adminFanfics = fanficsResult.data.fanfics;

            const usuariosNoAdmin = state.adminUsersData.filter(user => !user.isAdmin);

            if (usuariosNoAdmin.length === 0) {
                state.selectedAdminUserId = null;
            } else if (!usuariosNoAdmin.some(user => Number(user.id) === state.selectedAdminUserId)) {
                state.selectedAdminUserId = Number(usuariosNoAdmin[0].id);
            }

            adminUsers.innerHTML = renderAdminUsers(state.adminUsersData);
            renderAdminFanficsFiltrados();
        })
        .catch(error => {
            adminEstado.textContent = error.message;
            adminUsers.innerHTML = '<div class="mensaje error">No se pudieron cargar los usuarios</div>';
            adminFanfics.innerHTML = '<div class="mensaje error">No se pudieron cargar las entradas</div>';
            adminFanficsTitle.textContent = 'Entradas del usuario';
        });
}

function borrarFanficComoAdmin(fanficId) {
    if (!confirm('¿Seguro que quieres borrar esta entrada desde el panel admin?')) {
        return;
    }

    return solicitar('/api/admin/fanfics/eliminar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ fanficId })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo borrar la entrada');
            }

            adminEstado.textContent = 'Entrada borrada desde el panel admin.';
            return cargarPanelAdmin();
        })
        .catch(error => {
            adminEstado.textContent = error.message;
        });
}

function borrarUsuarioComoAdmin(userId) {
    const user = (state.adminUsersData || []).find(item => Number(item.id) === Number(userId));
    if (!user) {
        adminEstado.textContent = 'No se encontro la cuenta seleccionada.';
        return;
    }

    if (!confirm(`¿Seguro que quieres borrar la cuenta ${user.username} y todos sus fanfics?`)) {
        return;
    }

    return solicitar('/api/admin/users/eliminar', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify({ userId })
    })
        .then(({ response, data }) => {
            if (!response.ok || !data.ok) {
                throw new Error(data.detalle || data.mensaje || 'No se pudo borrar la cuenta');
            }

            state.selectedAdminUserId = null;
            adminEstado.textContent = 'Cuenta borrada desde el panel admin.';
            return cargarPanelAdmin();
        })
        .catch(error => {
            adminEstado.textContent = error.message;
        });
}

function actualizarSesion(user) {
    state.user = user;
    appSection.classList.toggle('hidden', !user);
    authSection.classList.toggle('hidden', Boolean(user));
    adminSection.classList.toggle('hidden', !user || !user.isAdmin);
    importSection.classList.toggle('hidden', !user || user.isAdmin);
    bibliotecaSection.classList.toggle('hidden', !user || user.isAdmin);
    estadisticasSection.classList.toggle('hidden', !user || user.isAdmin);

    if (user) {
        authStatus.innerHTML = `
            <div class="session-pill">
                <span class="session-pill__label">Conectada como</span>
                <strong>${escapeHtml(user.username)}${user.isAdmin ? ' · Admin' : ''}</strong>
            </div>
            <button class="boton-ghost" type="button" data-action="logout">Cerrar sesion</button>
        `;
        contadorFanfics.textContent = '';
    } else {
        cambiarVistaAuth('register');
        state.fanfics = [];
        state.currentPage = 1;
        state.adminFanfics = [];
        state.adminUsersData = [];
        state.selectedAdminUserId = null;
        authStatus.innerHTML = `
            <button class="account-button" type="button" data-action="open-auth-register">
                <span class="session-pill__label">Cuenta</span>
                <strong>Conectada como invitada</strong>
            </button>
        `;
        listaFanfics.innerHTML = '';
        paginacionFanfics.innerHTML = '';
        stats.innerHTML = '';
        adminUsers.innerHTML = '';
        adminFanfics.innerHTML = '';
        adminFanficsTitle.textContent = 'Entradas del usuario';
        adminSelectedActions.innerHTML = '';
        adminEstado.textContent = '';
        contadorFanfics.textContent = '';
    }
}

function cambiarVistaAuth(view) {
    const showingRegister = view === 'register';
    registerCard.classList.toggle('hidden', !showingRegister);
    loginCard.classList.toggle('hidden', showingRegister);
    showRegisterButton.classList.toggle('is-active', showingRegister);
    showLoginButton.classList.toggle('is-active', !showingRegister);
    showRegisterButton.setAttribute('aria-selected', String(showingRegister));
    showLoginButton.setAttribute('aria-selected', String(!showingRegister));
}

document.addEventListener('click', evento => {
    const button = evento.target.closest('[data-action="open-auth-register"], [data-action="open-auth-login"]');
    if (!button || authStatus.contains(button)) {
        return;
    }

    abrirCuenta(button.dataset.action === 'open-auth-login' ? 'login' : 'register');
});

function renderPaginaBiblioteca() {
    const start = (state.currentPage - 1) * state.fanficsPerPage;
    const end = start + state.fanficsPerPage;
    const paginaActual = state.fanfics.slice(start, end);
    const totalPages = Math.ceil(state.fanfics.length / state.fanficsPerPage);

    listaFanfics.innerHTML = paginaActual.map(renderFanfic).join('');
    paginacionFanfics.innerHTML = totalPages > 1 ? renderPaginacion(totalPages) : '';
}

function renderPaginacion(totalPages) {
    const botones = Array.from({ length: totalPages }, (_, index) => {
        const page = index + 1;
        const activeClass = page === state.currentPage ? ' is-active' : '';

        return `
            <button
                class="paginacion-fanfics__boton${activeClass}"
                type="button"
                data-action="go-page"
                data-page="${page}"
                aria-label="Ir a la pagina ${page}"
            >${page}</button>
        `;
    }).join('');

    return `<div class="paginacion-fanfics__inner">${botones}</div>`;
}

function renderFanfic(fanfic) {
    return `
        <article class="fanfic-card" data-fanfic-id="${fanfic.id}">
            <div class="fanfic-card__top">
                <div>
                    <p class="fanfic-card__eyebrow">${escapeHtml(fanfic.ao3Rating)}</p>
                    <h3>${escapeHtml(fanfic.titulo)}</h3>
                    <p class="autor">por ${escapeHtml(fanfic.autor)}</p>
                </div>

                <div class="fanfic-card__summary">
                    <div class="stars">${'★'.repeat(fanfic.userStars)}${'☆'.repeat(5 - fanfic.userStars)}</div>
                    <button
                        class="boton-ghost boton-ghost--icon"
                        type="button"
                        data-action="toggle-details"
                        aria-controls="fanfic-details-${fanfic.id}"
                        aria-expanded="false"
                        aria-label="Ver detalles"
                        title="Ver detalles"
                    >+</button>
                </div>
            </div>

            <div class="fanfic-details" id="fanfic-details-${fanfic.id}">
                <div class="fanfic-details__inner">
                    <div class="meta-grid">
                        <p><strong>AO3 rating:</strong> ${escapeHtml(fanfic.ao3Rating)}</p>
                        <p><strong>Words:</strong> ${fanfic.wordCount.toLocaleString('es-ES')}</p>
                        <p><strong>Fecha terminada:</strong> ${escapeHtml(fanfic.finishedDate)}</p>
                        <p><strong>AO3 URL:</strong> <a href="${escapeHtml(fanfic.ao3Url)}" target="_blank" rel="noopener noreferrer">Abrir en AO3</a></p>
                    </div>

                    ${renderTagGroup('Warnings', fanfic.warnings)}
                    ${renderTagGroup('Parejas', fanfic.relationships)}
                    ${renderTagGroup('Fandoms', fanfic.fandoms)}
                    ${renderTagGroup('Categorias', fanfic.categories)}

                    <form class="edit-form" data-fanfic-id="${fanfic.id}">
                        <div class="edit-form__fields">
                            <label class="campo">
                                <span>Fecha terminada</span>
                                <input type="date" name="finishedDate" value="${escapeHtml(fanfic.finishedDate)}" required>
                            </label>
                            <label class="campo">
                                <span>Estrellas</span>
                                <select name="userStars" required>
                                    ${renderStarOptions(fanfic.userStars)}
                                </select>
                            </label>
                        </div>
                        <div class="edit-form__actions">
                            <button class="boton-secundario" type="submit">Guardar cambios</button>
                            <button class="danger-button" type="button" data-action="delete-fanfic" data-fanfic-id="${fanfic.id}">Borrar entrada</button>
                        </div>
                        <p class="edit-form__feedback"></p>
                    </form>
                </div>
            </div>
        </article>
    `;
}

function renderAdminUsers(users) {
    if (!users || users.length === 0) {
        return '<div class="mensaje neutro">No hay usuarios creados todavia.</div>';
    }

    const usuariosNormales = users.filter(user => !user.isAdmin);

    if (usuariosNormales.length === 0) {
        return '<div class="mensaje neutro">No hay cuentas normales creadas todavia.</div>';
    }

    return usuariosNormales.map(user => `
        <button
            class="admin-row admin-row--button${Number(user.id) === state.selectedAdminUserId ? ' is-active' : ''}"
            type="button"
            data-action="select-admin-user"
            data-user-id="${user.id}"
        >
            <div>
                <p class="admin-row__title">${escapeHtml(user.username)}</p>
                <p class="admin-row__meta">${escapeHtml(user.email)}</p>
            </div>
            <div class="admin-row__aside">
                <strong>${user.fanficCount}</strong>
                <span>fanfics</span>
            </div>
        </button>
    `).join('');
}

function renderAdminFanfics(fanfics) {
    if (!fanfics || fanfics.length === 0) {
        return '<div class="mensaje neutro">Ese usuario no tiene entradas registradas.</div>';
    }

    return fanfics.map(fanfic => `
        <article class="admin-row">
            <div>
                <p class="admin-row__title">${escapeHtml(fanfic.titulo)}</p>
                <p class="admin-row__meta">por ${escapeHtml(fanfic.autor)} · cuenta ${escapeHtml(fanfic.ownerUsername)} · ${escapeHtml(fanfic.ao3Rating)}</p>
            </div>
            <button
                class="danger-button danger-button--small"
                type="button"
                data-action="admin-delete-fanfic"
                data-fanfic-id="${fanfic.id}"
            >Borrar</button>
        </article>
    `).join('');
}

function renderAdminFanficsFiltrados() {
    const users = state.adminUsersData || [];
    const selectedUser = users.find(user => Number(user.id) === state.selectedAdminUserId);

    if (!selectedUser) {
        adminFanficsTitle.textContent = 'Entradas del usuario';
        adminSelectedActions.innerHTML = '';
        adminFanfics.innerHTML = '<div class="mensaje neutro">Selecciona un usuario para ver sus entradas.</div>';
        return;
    }

    adminFanficsTitle.textContent = `Entradas de ${selectedUser.username}`;
    adminSelectedActions.innerHTML = `
        <button
            class="danger-button danger-button--small"
            type="button"
            data-action="admin-delete-user"
            data-user-id="${selectedUser.id}"
        >Borrar cuenta</button>
    `;

    const fanficsFiltrados = state.adminFanfics.filter(
        fanfic => Number(fanfic.ownerUserId) === Number(state.selectedAdminUserId)
    );

    adminFanfics.innerHTML = renderAdminFanfics(fanficsFiltrados);
}

function renderStarOptions(actual) {
    return [5, 4, 3, 2, 1].map(valor => `
        <option value="${valor}" ${valor === actual ? 'selected' : ''}>${valor} estrella${valor === 1 ? '' : 's'}</option>
    `).join('');
}

function renderTagGroup(titulo, items) {
    if (!items || items.length === 0) {
        return `
            <div class="tag-group">
                <p class="tag-title">${titulo}</p>
                <p class="tag-empty">Sin datos</p>
            </div>
        `;
    }

    return `
        <div class="tag-group">
            <p class="tag-title">${titulo}</p>
            <div class="tags">
                ${items.map(item => `<span class="tag">${escapeHtml(item)}</span>`).join('')}
            </div>
        </div>
    `;
}

function renderStats(datos) {
    return `
        <div class="stats-grid">
            <article class="stat-card">
                <h3>Total</h3>
                <p class="stat-big">${datos.totalFanfics}</p>
            </article>
            <article class="stat-card">
                <h3>Media de palabras</h3>
                <p class="stat-big">${Number(datos.averageWords).toLocaleString('es-ES')}</p>
            </article>
        </div>

        <div class="stats-lists">
            ${renderRanking('Parejas mas leidas', datos.topRelationships)}
            ${renderRanking('Fandoms mas leidos', datos.topFandoms)}
            ${renderRanking('Warnings mas comunes', datos.topWarnings)}
        </div>

        <div class="stats-lists">
            ${renderBreakdown('AO3 ratings', datos.ao3Ratings)}
            ${renderBreakdown('Tus estrellas', datos.userStars)}
            ${renderBreakdown('Categorias', datos.categories)}
        </div>
    `;
}

function renderRanking(titulo, items) {
    const contenido = !items || items.length === 0
        ? '<li>Sin datos</li>'
        : items.map(item => `<li><span>${escapeHtml(item.name)}</span><strong>${item.count}</strong></li>`).join('');

    return `
        <article class="ranking-card">
            <h3>${titulo}</h3>
            <ol>${contenido}</ol>
        </article>
    `;
}

function renderBreakdown(titulo, mapa) {
    const entries = Object.entries(mapa || {});
    const contenido = entries.length === 0
        ? '<li>Sin datos</li>'
        : entries.map(([clave, valor]) => `<li><span>${escapeHtml(clave)}</span><strong>${valor}</strong></li>`).join('');

    return `
        <article class="ranking-card">
            <h3>${titulo}</h3>
            <ul>${contenido}</ul>
        </article>
    `;
}

function solicitar(url, options = {}) {
    return fetch(url, {
        credentials: 'same-origin',
        ...options
    })
        .then(response => response.json()
            .catch(() => ({}))
            .then(data => {
                if (response.status === 401) {
                    actualizarSesion(null);
                }

                return { response, data };
            }))
        .catch(error => Promise.reject(new Error(error.message || 'No se pudo completar la peticion')));
}

function escapeHtml(valor) {
    return String(valor ?? '')
        .replaceAll('&', '&amp;')
        .replaceAll('<', '&lt;')
        .replaceAll('>', '&gt;')
        .replaceAll('"', '&quot;')
        .replaceAll("'", '&#39;');
}

init();
