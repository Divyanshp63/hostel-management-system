import React, { useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import AlertMessage from '../../components/common/AlertMessage';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [loading, setLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState('');

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  const queryParams = new URLSearchParams(location.search);
  const sessionExpired = queryParams.get('expired') === 'true';

  const handleSubmit = async (e) => {
    e.preventDefault();
    setErrorMessage('');

    if (!email || !password) {
      setErrorMessage('Please enter both email and password.');
      return;
    }

    setLoading(true);
    const result = await login(email, password);
    setLoading(false);

    if (result.success) {
      if (result.role === 'STUDENT') {
        navigate('/student/dashboard');
      } else {
        navigate('/admin/dashboard');
      }
    } else {
      setErrorMessage(result.message);
    }
  };

  const handleQuickLogin = (demoEmail, demoPassword) => {
    setEmail(demoEmail);
    setPassword(demoPassword);
    setErrorMessage('');
  };

  return (
    <div className="min-vh-100 d-flex align-items-center justify-content-center bg-light py-5 px-3">
      <div className="card border-0 shadow-lg" style={{ maxWidth: '440px', width: '100%', borderRadius: '16px' }}>
        <div className="card-body p-4 p-sm-5">
          <div className="text-center mb-4">
            <div className="d-inline-flex align-items-center justify-content-center bg-primary text-white rounded-circle mb-3" style={{ width: '56px', height: '56px' }}>
              <i className="bi bi-buildings-fill fs-3"></i>
            </div>
            <h3 className="fw-bold text-dark mb-1">Welcome Back</h3>
            <p className="text-secondary small">Sign in to your HostelOps account</p>
          </div>

          {sessionExpired && (
            <AlertMessage
              type="warning"
              message="Your session has expired. Please log in again to continue."
              onClose={() => {}}
            />
          )}

          {errorMessage && (
            <AlertMessage
              type="danger"
              message={errorMessage}
              onClose={() => setErrorMessage('')}
            />
          )}

          <form onSubmit={handleSubmit}>
            <div className="mb-3">
              <label className="form-label small fw-semibold text-secondary">Email Address</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0">
                  <i className="bi bi-envelope text-muted"></i>
                </span>
                <input
                  type="email"
                  className="form-control border-start-0"
                  placeholder="name@hostel.com"
                  value={email}
                  onChange={(e) => setEmail(e.target.value)}
                  required
                />
              </div>
            </div>

            <div className="mb-4">
              <label className="form-label small fw-semibold text-secondary">Password</label>
              <div className="input-group">
                <span className="input-group-text bg-white border-end-0">
                  <i className="bi bi-lock text-muted"></i>
                </span>
                <input
                  type={showPassword ? 'text' : 'password'}
                  className="form-control border-start-0 border-end-0"
                  placeholder="&bull;&bull;&bull;&bull;&bull;&bull;&bull;&bull;"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                />
                <button
                  type="button"
                  className="input-group-text bg-white border-start-0 text-muted"
                  style={{ cursor: 'pointer' }}
                  onClick={() => setShowPassword(!showPassword)}
                  title={showPassword ? 'Hide password' : 'Show password'}
                  aria-label="Toggle password visibility"
                >
                  <i className={`bi ${showPassword ? 'bi-eye-slash-fill' : 'bi-eye-fill'}`}></i>
                </button>
              </div>
            </div>

            <button
              type="submit"
              className="btn btn-primary w-100 py-2 fw-semibold shadow-sm mb-3"
              disabled={loading}
            >
              {loading ? (
                <>
                  <span className="spinner-border spinner-border-sm me-2" role="status"></span>
                  Authenticating...
                </>
              ) : (
                'Sign In'
              )}
            </button>
          </form>

          {/* Quick Demo Credentials Box */}
          <div className="p-3 bg-light rounded-3 border mb-3">
            <div className="small fw-bold text-dark mb-2">
              <i className="bi bi-lightning-charge-fill text-warning me-1"></i> Quick Demo Fill:
            </div>
            <div className="d-flex gap-2 flex-wrap">
              <button
                type="button"
                className="btn btn-sm btn-outline-dark"
                onClick={() => handleQuickLogin('admin@hostel.com', 'Admin@123')}
              >
                Admin
              </button>
              <button
                type="button"
                className="btn btn-sm btn-outline-dark"
                onClick={() => handleQuickLogin('accountant@hostel.com', 'Accountant@123')}
              >
                Accountant
              </button>
              <button
                type="button"
                className="btn btn-sm btn-outline-primary"
                onClick={() => handleQuickLogin('aarav.patel@hostel.com', 'Student@123')}
              >
                <i className="bi bi-person-fill me-1"></i> Student
              </button>
            </div>
          </div>

          <div className="text-center small text-secondary">
            Don't have an account?{' '}
            <Link to="/register" className="text-primary text-decoration-none fw-semibold">
              Register as Student
            </Link>
          </div>
        </div>
      </div>
    </div>
  );
};

export default Login;
