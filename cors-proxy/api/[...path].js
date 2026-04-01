const TARGETS = {
  cmc: "https://pro-api.coinmarketcap.com",
  mexc: "https://api.mexc.com",
};

export default async function handler(req) {
  if (req.method === "OPTIONS") {
    return new Response(null, { status: 204, headers: corsHeaders() });
  }

  const url = new URL(req.url);
  const segments = url.pathname.replace(/^\/api\//, "").split("/");
  const target = segments[0];
  const apiPath = "/" + segments.slice(1).join("/");

  const base = TARGETS[target];
  if (!base) {
    return new Response(JSON.stringify({ error: "Unknown target: " + target }), {
      status: 400,
      headers: { "Content-Type": "application/json", ...corsHeaders() },
    });
  }

  const targetUrl = base + apiPath + url.search;

  const headers = new Headers();
  for (const [key, value] of req.headers.entries()) {
    if (key.startsWith("x-") || key === "content-type" || key === "accept") {
      headers.set(key, value);
    }
  }

  const response = await fetch(targetUrl, {
    method: req.method,
    headers,
    body: req.method !== "GET" && req.method !== "HEAD" ? await req.text() : undefined,
  });

  const responseHeaders = corsHeaders();
  responseHeaders["Content-Type"] =
    response.headers.get("Content-Type") || "application/json";

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
