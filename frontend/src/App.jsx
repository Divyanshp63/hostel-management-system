import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import ProtectedRoute from './components/common/ProtectedRoute';
import DashboardLayout from './components/layout/DashboardLayout';

// Public & Error Pages
import LandingPage from './pages/LandingPage';
import Login from './pages/auth/Login';
import Register from './pages/auth/Register';
import Unauthorized from './pages/error/Unauthorized';
import NotFound from './pages/error/NotFound';

// Admin Pages
import AdminDashboard from './pages/admin/AdminDashboard';
import StudentManagement from './pages/admin/StudentManagement';
import RoomManagement from './pages/admin/RoomManagement';
import RoomAllocationManagement from './pages/admin/RoomAllocationManagement';
import FeeManagement from './pages/admin/FeeManagement';
import PaymentManagement from './pages/admin/PaymentManagement';
import ComplaintManagement from './pages/admin/ComplaintManagement';
import VisitorManagement from './pages/admin/VisitorManagement';
import LeaveManagement from './pages/admin/LeaveManagement';
import MessMenuManagement from './pages/admin/MessMenuManagement';
import NoticeManagement from './pages/admin/NoticeManagement';

// Student Pages
import StudentDashboard from './pages/student/StudentDashboard';
import StudentRoom from './pages/student/StudentRoom';
import StudentFees from './pages/student/StudentFees';
import StudentComplaints from './pages/student/StudentComplaints';
import StudentLeaves from './pages/student/StudentLeaves';
import StudentVisitors from './pages/student/StudentVisitors';
import StudentMessMenu from './pages/student/StudentMessMenu';
import StudentNotices from './pages/student/StudentNotices';
import StudentProfile from './pages/student/StudentProfile';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          {/* Public Routes */}
          <Route path="/" element={<LandingPage />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/unauthorized" element={<Unauthorized />} />

          {/* ================= ADMIN PROTECTED ROUTES ================= */}
          <Route
            path="/admin"
            element={
              <ProtectedRoute allowedRoles={['ADMIN', 'ACCOUNTANT']}>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/admin/dashboard" replace />} />
            <Route path="dashboard" element={<AdminDashboard />} />
            <Route path="students" element={<StudentManagement />} />
            <Route path="rooms" element={<RoomManagement />} />
            <Route path="allocations" element={<RoomAllocationManagement />} />
            <Route path="fees" element={<FeeManagement />} />
            <Route path="payments" element={<PaymentManagement />} />
            <Route path="complaints" element={<ComplaintManagement />} />
            <Route path="visitors" element={<VisitorManagement />} />
            <Route path="leaves" element={<LeaveManagement />} />
            <Route path="mess-menu" element={<MessMenuManagement />} />
            <Route path="notices" element={<NoticeManagement />} />
          </Route>

          {/* ================= STUDENT PROTECTED ROUTES ================= */}
          <Route
            path="/student"
            element={
              <ProtectedRoute allowedRoles={['STUDENT']}>
                <DashboardLayout />
              </ProtectedRoute>
            }
          >
            <Route index element={<Navigate to="/student/dashboard" replace />} />
            <Route path="dashboard" element={<StudentDashboard />} />
            <Route path="room" element={<StudentRoom />} />
            <Route path="fees" element={<StudentFees />} />
            <Route path="complaints" element={<StudentComplaints />} />
            <Route path="leaves" element={<StudentLeaves />} />
            <Route path="visitors" element={<StudentVisitors />} />
            <Route path="mess-menu" element={<StudentMessMenu />} />
            <Route path="notices" element={<StudentNotices />} />
            <Route path="profile" element={<StudentProfile />} />
          </Route>

          {/* 404 Fallback */}
          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
