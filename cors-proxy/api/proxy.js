export default async function handler(req) {
  const url = new URL(req.url);
  const target = url.searchParams.get("url");

  if (!target) {
    return new Response(JSON.stringify({ error: "Missing 'url' parameter" }), {
      status: 400,
      headers: { "Content-Type": "application/json", ...corsHeaders() },
    });
  }

  // Only allow MEXC and CMC APIs
  const allowed = ["https://api.mexc.com/", "https://pro-api.coinmarketcap.com/"];
  if (!allowed.some((prefix) => target.startsWith(prefix))) {
    return new Response(JSON.stringify({ error: "Target URL not allowed" }), {
      status: 403,
      headers: { "Content-Type": "application/json", ...corsHeaders() },
    });
  }

  // Handle CORS preflight
  if (req.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders() });
  }

  // Forward the request
  const headers = new Headers();
  for (const [key, value] of req.headers.entries()) {
    // Forward relevant headers
    if (
      key.startsWith("x-") ||
      key === "content-type" ||
      key === "accept"
    ) {
      headers.set(key, value);
    }
  }

  const response = await fetch(target, {
    method: req.method,
    headers,
    body: req.method !== "GET" && req.method !== "HEAD" ? await req.text() : undefined,
  });

  const responseHeaders = corsHeaders();
  responseHeaders["Content-Type"] = response.headers.get("Content-Type") || "application/json";

  return new Response(await response.text(), {
    status: response.status,
    headers: responseHeaders,
  });
}

function corsHeaders() {
  return {
    "Access-Control-Allow-Origin": "*",
    "Access-Control-Allow-Methods": "GET, POST, OPTIONS",
    "Access-Control-Allow-Headers": "Content-Type, X-CMC_PRO_API_KEY, X-MEXC-APIKEY",
  };
}

export const config = { runtime: "edge" };
