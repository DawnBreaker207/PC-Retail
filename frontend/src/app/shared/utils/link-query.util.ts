export function splitLinkQuery(link: string): { path: string; queryParams: Record<string, string> } {
  const [path, query] = link.split('?');
  return {
    path,
    queryParams: query ? Object.fromEntries(new URLSearchParams(query)) : {},
  };
}
