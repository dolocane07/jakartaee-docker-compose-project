const formBuscador = document.getElementById('formBuscador');
const textoBusqueda = document.getElementById('textoBusqueda');
const estado = document.getElementById('estado');
const resultados = document.getElementById('resultados');

formBuscador.addEventListener('submit', async (evento) => {
    evento.preventDefault();

    const texto = textoBusqueda.value.trim();
    resultados.innerHTML = '';
    estado.textContent = 'Buscando contactos...';

    try {
        const respuesta = await fetch('api/buscar-contactos', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ texto })
        });

        const datos = await respuesta.json();

        if (!respuesta.ok || !datos.ok) {
            throw new Error(datos.mensaje || 'Error al realizar la búsqueda');
        }

        estado.textContent = `Se han encontrado ${datos.total} contacto(s).`;

        if (datos.total === 0) {
            resultados.innerHTML = '<div class="vacio">No hay contactos que coincidan con la búsqueda.</div>';
            return;
        }

        resultados.innerHTML = datos.resultados.map(contacto => `
            <article class="resultado">
                <h2>${contacto.nomCon}</h2>
                <p><strong>ID:</strong> ${contacto.ideCon}</p>
                <p><strong>Teléfono:</strong> ${contacto.tlfCon}</p>
            </article>
        `).join('');

    } catch (error) {
        estado.textContent = '';
        resultados.innerHTML = `<div class="error">${error.message}</div>`;
    }
});
