import React, { useEffect } from 'react';
import axios from 'axios';

import {

  BrowserRouter as Router,

  Routes,

  Route,

  Navigate,

  useLocation,

} from 'react-router-dom';
 
import Dashboard from './components/SegmentDashboard';

import SegmentDefine from './components/SegmentDefine';

import RateMaker from './components/RateMaker';

import RateChecker from './components/RateChecker';

import SpeclRateReq from './components/SpeclRateReq';

import About from './components/About';

import AppNavbar from './components/Navbar';

import Login from './components/Login';
import Unauthorized from './components/Unauthorized';

import { isTokenExpired } from './util/jwtUtils';

import config from './config/config';
 
// ✅ Protects routes that require authentication

const PrivateRoute = ({ children, allowedRoles }) => {
  const token = localStorage.getItem('token');
  const roles = JSON.parse(localStorage.getItem('roles')) || [];

  if (!token || isTokenExpired(token)) {
    localStorage.removeItem('token');
    return <Navigate to="/" replace />;
  }

  // Check if the route requires specific roles
  if (allowedRoles && allowedRoles.length > 0) {
    const hasAccess = roles.some(role => allowedRoles.includes(role));
    if (!hasAccess) {
      return <Navigate to="/unauthorized" replace />;
    }
  }

  return children;
};

 
// ✅ Layout wrapper

const Layout = () => {

  const location = useLocation();

  const hideNavbar = location.pathname === '/'; // hide navbar on login

  // Prime CSRF cookie on app load
  useEffect(() => {
    // Call backend directly to ensure the CSRF cookie is set on the backend host
    const base = new URL(config.RATES_API_BASE);
    const origin = `${base.protocol}//${base.host}`;
    const pingUrl = `${origin}/api/exrates/ping`;
    const csrfUrl = `${origin}/api/csrf`;

    axios.get(pingUrl, { withCredentials: true })
      .then(() => axios.get(csrfUrl, { withCredentials: true }))
      .then(res => {
        const token = res?.data?.token;
        if (token) {
          axios.defaults.headers.common['X-XSRF-TOKEN'] = token;
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    window.history.pushState(null,'',window.location.href);
    window.onpopstate = function(){
      if(!localStorage.getItem('token')){
        window.location.href = '/ratemgt/';
      }else{
        window.history.go(1);
      }
    };
  },[]);
 
  return (
    <div className="d-flex">

          {!hideNavbar && <AppNavbar />}
      <div style={{ flexGrow: 1 }}>
        <Routes>
        <Route path="/" element={<Login />} />

             <Route
                path="/maker"
                element={
                  <PrivateRoute allowedRoles={['USER']}>
                    <RateMaker />
                  </PrivateRoute>
                }
              />

              {/* Checker only */}
              <Route
                path="/checker"
                element={
                  <PrivateRoute allowedRoles={['ADMIN']}>
                    <RateChecker />
                  </PrivateRoute>
                }
              />
             {/* Common routes (accessible by any logged-in user) */}
            <Route
              path="/segment/dashboard"
              element={
                <PrivateRoute>
                  <Dashboard />
                </PrivateRoute>
              }
            />
            <Route
              path="/segment/define"
              element={
                <PrivateRoute>
                  <SegmentDefine />
                </PrivateRoute>
              }
            />
            <Route
              path="/SpeclRateReq"
              element={
                <PrivateRoute>
                  <SpeclRateReq />
                </PrivateRoute>
              }
            />

            {/* Unauthorized route */}
            <Route path="/unauthorized" element={<Unauthorized />} />

            <Route

                path="/about"

                element={<PrivateRoute><About /></PrivateRoute>}

              />
    
              {/* 👇 Fallback for unknown routes */}
            <Route path="*" element={<Navigate to="/" replace />} />
        </Routes>
      </div>
    </div>

  );

};
 
// ✅ Main App with basename "/ratemgt"

const App = () => (
  <Router basename="/ratemgt">
  <Layout />
  </Router>

);
 
export default App;