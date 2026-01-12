import React from 'react';
import { Navigate } from 'react-router-dom';

const ProtectedRoute = ({ allowedRoles, children }) => {
  const roles = JSON.parse(localStorage.getItem('roles')) || [];

  const hasAccess = roles.some(role => allowedRoles.includes(role));

  if (!hasAccess) {
    // redirect to unauthorized page
    return <Navigate to="/unauthorized" replace />;
  }

  return children;
};

export default ProtectedRoute;