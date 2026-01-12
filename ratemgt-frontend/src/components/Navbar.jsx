import React, { useState, useEffect } from 'react';
import { Nav, Button } from 'react-bootstrap';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { FaChevronDown, FaChevronUp } from 'react-icons/fa';

const AppNavbar = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const [user, setUser] = useState(null);
  const [roles, setRoles] = useState([]);
  const [segmentationExpanded, setSegmentationExpanded] = useState(true);
  const [rateMgtExpanded, setRateMgtExpanded] = useState(true);

  useEffect(() => {
    const storedUser = localStorage.getItem('user');
    if (storedUser) {
      setUser(JSON.parse(storedUser));
    }

    const storedRoles = localStorage.getItem('roles');
    if (storedRoles) {
      try {
        setRoles(JSON.parse(storedRoles));
      } catch (e) {
        console.error('Invalid roles in localStorage');
        setRoles([]);
      }
    }
  }, []);

  const isAdmin = roles.includes('ADMIN');
  const isUser = roles.includes('USER');
  const isNonUser = roles.includes('NONUSER');

  const handleLogout = () => {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('roles');
    navigate('/',{replace:true});
    window.location.reload();
  };

  return (
    <nav
      className="sidebar bg-white shadow-lg d-flex flex-column justify-content-between p-3"
      style={{
        width: '300px',
        height: '100vh',
        position: 'fixed',
        top: 0,
        left: 0,
        zIndex: 1000,
      }}
    >
      <div>
        {/* Logo */}
        <div className="mb-4 text-center d-flex align-items-center justify-content-center gap-2">
          <img
            src="/images/logo.png"
            alt="Sampath Logo"
            style={{ height: '60px', marginRight: '0.5rem' }}
          />
          <h4 style={{ color: '#f47b20' }} className="fw-bold text-uppercase mb-0">
            Sampath Bank Portal
          </h4>
        </div>
        <hr/>
        {/* Logged in user */}
        {user && (
          <div className="text-center small text-muted mt-3 mb-3 px-3">
            <span className="text-muted small mb-1">Logged in as:</span> &nbsp;
            <span className="fw-semibold text-dark fs-20">
              {user.charAt(0).toUpperCase() + user.toLowerCase().slice(1)}
            </span>
          </div>
        )}

        <hr/>

        {/* Sidebar Navigation */}
        <Nav className="flex-column">
          {/* ADMIN: Customer Segmentation */}
          {isNonUser && (
            <div className="mb-3">
              <div
                className="d-flex justify-content-between align-items-center px-3 py-2 text-primary fw-semibold"
                style={{ cursor: 'pointer' }}
                onClick={() => setSegmentationExpanded(!segmentationExpanded)}
              >
                <span>Customer Segmentation</span>
                {segmentationExpanded ? <FaChevronUp /> : <FaChevronDown />}
              </div>
              {segmentationExpanded && (
                <div className="ps-4">
                  <Nav.Link
                    as={Link}
                    to="/segment/dashboard"
                    className={`mb-2 rounded px-3 py-2 ${
                      location.pathname === '/segment/dashboard'
                        ? 'bg-primary text-white'
                        : 'text-dark'
                    }`}
                  >
                    Report Dashboard
                  </Nav.Link>
                  <Nav.Link
                    as={Link}
                    to="/segment/define"
                    className={`mb-2 rounded px-3 py-2 ${
                      location.pathname === '/segment/define'
                        ? 'bg-primary text-white'
                        : 'text-dark'
                    }`}
                  >
                    Customize Segment
                  </Nav.Link>
                </div>
              )}
            </div>
          )}

          {/* USER: Rate Management */}
         
            <div className="mb-3">
              <div
                className="d-flex justify-content-between align-items-center px-3 py-2 text-primary fw-semibold"
                style={{ cursor: 'pointer' }}
                onClick={() => setRateMgtExpanded(!rateMgtExpanded)}
              >
                <span>Rate Management</span>
                {rateMgtExpanded ? <FaChevronUp /> : <FaChevronDown />}
              </div>
              {rateMgtExpanded && (
                <div className="ps-4">

                {isUser && (
                  <Nav.Link
                    as={Link}
                    to="/maker"
                    className={`mb-2 rounded px-3 py-2 ${
                      location.pathname === '/maker'
                        ? 'bg-primary text-white'
                        : 'text-dark'
                    }`}
                  >
                    Maker
                  </Nav.Link>
                  )}
                  {isAdmin && (
                  <Nav.Link
                    as={Link}
                    to="/checker"
                    className={`mb-2 rounded px-3 py-2 ${
                      location.pathname === '/checker'
                        ? 'bg-primary text-white'
                        : 'text-dark'
                    }`}
                  >
                    Checker
                  </Nav.Link>
                  )}
                </div>
              )}
            </div>
          

          {/* Admin: Other Pages */}
          {isNonUser && (
            <>
              <Nav.Link
                as={Link}
                to="/speclratereq"
                className={`mb-2 rounded px-3 py-2 ${
                  location.pathname === '/speclratereq'
                    ? 'bg-primary text-white'
                    : 'text-dark'
                }`}
              >
                Special Rate Request
              </Nav.Link>
              <Nav.Link
                as={Link}
                to="/about"
                className={`mb-2 rounded px-3 py-2 ${
                  location.pathname === '/about' ? 'bg-primary text-white' : 'text-dark'
                }`}
              >
                About
              </Nav.Link>
            </>
          )}  
        </Nav>

        
      </div>
     
     

      <div className="text-left small text-muted mt-3">
        {/* Logout Button */}
        <Nav className="ms-auto mt-4">
          <Button variant="outline-danger" onClick={handleLogout}>
            Logout
          </Button>
        </Nav>
      </div>
      <hr/>
      <div className="text-center small text-muted mt-3">
        ©{new Date().getFullYear()} MillenniumIT ESP (Pvt) Ltd.
      </div>
    </nav>
  );
};

export default AppNavbar;
