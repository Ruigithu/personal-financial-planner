import { BrowserRouter as Router, Routes, Route, Navigate } from 'react-router-dom';
import { ToastContainer } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

import Navbar from './components/Navbar.tsx';
import Login from './pages/login';
import Register from './pages/Register';
import Dashboard from './pages/Dashboard';
import Profile from './pages/Profile';
import FinancialPlanForm from './pages/FinancialPlanForm.tsx';
import FinancialPlanDetail from './pages/FinancialPlanDetails';
import Timeline from './pages/TimeLine';
import AdvisorList from './pages/AdvisorList';
import AdvisorDetail from './pages/AdvisorDetails';
import BookAppointment from './pages/BookAppointment';
import AppointmentDetail from './pages/AppointmentDetails';
import Meeting from './pages/Meeting';
import PrivateRoute from './components/PrivateRoute';

function App() {
  return (
      <div className="app">
        <Navbar />
        <main className="container">
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />

            {/* Protected User Routes */}
            <Route path="/dashboard" element={
              <PrivateRoute>
                <Dashboard />
              </PrivateRoute>
            } />
            <Route path="/profile" element={
              <PrivateRoute>
                <Profile />
              </PrivateRoute>
            } />
            <Route path="/create-plan" element={
              <PrivateRoute>
                <FinancialPlanForm />
              </PrivateRoute>
            } />
            <Route path="/plan/:id" element={
              <PrivateRoute>
                <FinancialPlanDetail />
              </PrivateRoute>
            } />
            <Route path="/timeline" element={
              <PrivateRoute>
                <Timeline />
              </PrivateRoute>
            } />
            <Route path="/advisors" element={
              <PrivateRoute>
                <AdvisorList />
              </PrivateRoute>
            } />
            <Route path="/advisor/:id" element={
              <PrivateRoute>
                <AdvisorDetail />
              </PrivateRoute>
            } />
            <Route path="/book-appointment/:advisorId" element={
              <PrivateRoute>
                <BookAppointment />
              </PrivateRoute>
            } />
            <Route path="/appointment/:id" element={
              <PrivateRoute>
                <AppointmentDetail />
              </PrivateRoute>
            } />
            <Route path="/meeting/:id" element={
              <PrivateRoute>
                <Meeting />
              </PrivateRoute>
            } />

          </Routes>
        </main>
        <ToastContainer position="bottom-right" />
      </div>
  );
}

export default App;