async function securedFetch(input, init = {}) {
    const response = await fetch(input, init);

    // Detectar si Spring redirige a login (HTML en vez de JSON)
    const contentType = response.headers.get("content-type");

    if (response.status === 200 && contentType && contentType.includes("text/html")) {
        const text = await response.text();
        if (text.includes("Iniciar sesión") || text.includes("name=\"password\"")) {
            // Heurística: estamos en login, redirigir
            window.location.href = "/login";
            return null;
        }
    }

    return response;
}
