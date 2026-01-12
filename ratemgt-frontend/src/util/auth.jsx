export const getUserRoles = () => {
  const roles = localStorage.getItem('roles');
  return roles ? JSON.parse(roles) : [];
};
