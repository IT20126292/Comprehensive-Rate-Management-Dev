export const isTokenExpired = (token) => {
  if (!token) return true;
  try {
    const [, payload] = token.split('.');
    const decoded = JSON.parse(atob(payload));
    const exp = decoded.exp;
    return Date.now() >= exp * 1000;
  } catch (e) {
    return true;
  }
};
