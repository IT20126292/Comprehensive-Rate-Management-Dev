import React, { useState } from 'react';
import axios from 'axios';
import { useNavigate } from 'react-router-dom';
import { Container, Form, Button, Alert, Card } from 'react-bootstrap';
import config from '../config/config';
import {authLogin} from '../api/api.js';

const AUTH_BASE = config.API_BASE;


const Login = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState({ username: '', password: '' });
  const [error, setError] = useState('');

  React.useEffect(()=>{
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    localStorage.removeItem('roles');
    window.history.pushState(null,'',window.location.href);
    window.onpopstate = function () {
      this.window.location.href = '/ratemgt/';
    };
  },[]);

 const handleChange = (e) => {
    setForm({ ...form, [e.target.name]: e.target.value });
  };

  const handleLogin = async (e) => {
  e.preventDefault();
  setError('');

  try {
    // Clean the username only when submitting (remove all spaces)
    const cleanedForm = {
      ...form,
      username: form.username.replace(/\s+/g, ''),
    };

    const res = await authLogin(cleanedForm);
    console.log(res);

    localStorage.setItem('token', res.data.accessToken);
    localStorage.setItem('user', JSON.stringify(res.data.username));
    localStorage.setItem('roles', JSON.stringify(res.data.roles));

    const roles = localStorage.getItem('roles');
    console.log('Logged User:', localStorage.getItem('user'));
    console.log('Logged Roles:', roles);
    console.log('Logged Time:', new Date());

    if (!roles || roles.length === 0) {
      navigate('/');
    } else if (roles.includes('ADMIN')) {
      navigate('/checker');
    } else if (roles.includes('USER')) {
      navigate('/maker');
    } else if (roles.includes('NONUSER')) {
      navigate('/segment/dashboard');
    } else {
      navigate('/');
    }

  } catch (err) {
    setError('Invalid credentials. Please try again.');
  }
};

  return (
    <Container className="d-flex align-items-center justify-content-center" style={{ height: '100vh', marginTop: -100}}>
      <Card className="p-4 shadow-lg" style={{ minWidth: '400px' }}>
        <img
          src="/images/logo.png"
          alt="Sampath Logo"
          className="mx-auto d-block"
          style={{ height: '60px', width: '60px' }}
        />
        <h4 className="text-center text-primary mb-3">Login to Sampath Portal</h4>
        {error && <Alert variant="danger">{error}</Alert>}
        <Form onSubmit={handleLogin}>
          <Form.Group className="mb-3">
            <Form.Label>Username</Form.Label>
            <Form.Control
              type="text"
              name="username"
              value={form.username}
              onChange={handleChange}
              required
              placeholder="Enter username"
            />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Password</Form.Label>
            <Form.Control
              type="password"
              name="password"
              value={form.password}
              onChange={handleChange}
              required
              placeholder="Enter password"
            />
          </Form.Group>
          <Button type="submit" variant="primary" className="w-100">
            Login
          </Button>
        </Form>
        <div className="text-center small text-muted mt-3">
            ©{new Date().getFullYear()} MillenniumIT ESP (Pvt) Ltd.
        </div>
      </Card>    
    </Container>
    
  );
  
};

export default Login;
