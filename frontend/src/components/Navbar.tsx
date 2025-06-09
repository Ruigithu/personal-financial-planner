
import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import authService from '../api/auth.service';
import { AppBar, Toolbar, Typography, Button, IconButton, Menu, MenuItem, Box } from '@mui/material';
import { AccountCircle, Menu as MenuIcon } from '@mui/icons-material';

const Navbar = () => {
  const navigate = useNavigate();
  const isAuthenticated = authService.isAuthenticated();
  const currentUser = authService.getCurrentUser();
  const role = authService.getRole();

  const [anchorEl, setAnchorEl] = useState<null | HTMLElement>(null);
  const [mobileMenuAnchorEl, setMobileMenuAnchorEl] = useState<null | HTMLElement>(null);

  const handleProfileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMobileMenuOpen = (event: React.MouseEvent<HTMLElement>) => {
    setMobileMenuAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
    setMobileMenuAnchorEl(null);
  };

  const handleLogout = () => {
    authService.logout();
    handleMenuClose();
    navigate('/login');
  };

  const renderMenu = (
    <Menu
      anchorEl={anchorEl}
      open={Boolean(anchorEl)}
      onClose={handleMenuClose}
    >
      <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }}>Profile</MenuItem>
      <MenuItem onClick={handleLogout}>Logout</MenuItem>
    </Menu>
  );

  const renderMobileMenu = (
    <Menu
      anchorEl={mobileMenuAnchorEl}
      open={Boolean(mobileMenuAnchorEl)}
      onClose={handleMenuClose}
    >
      {isAuthenticated ? (
        <>
          {role === 'USER' && (
            <>
              <MenuItem onClick={() => { handleMenuClose(); navigate('/dashboard'); }}>Dashboard</MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate('/create-plan'); }}>Create Plan</MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate('/timeline'); }}>Timeline</MenuItem>
              <MenuItem onClick={() => { handleMenuClose(); navigate('/advisors'); }}>Find Advisors</MenuItem>
            </>
          )}
          {role === 'ADVISOR' && (
            <MenuItem onClick={() => { handleMenuClose(); navigate('/advisor-dashboard'); }}>Dashboard</MenuItem>
          )}
          <MenuItem onClick={() => { handleMenuClose(); navigate('/profile'); }}>Profile</MenuItem>
          <MenuItem onClick={handleLogout}>Logout</MenuItem>
        </>
      ) : (
        <>
          <MenuItem onClick={() => { handleMenuClose(); navigate('/login'); }}>Login</MenuItem>
          <MenuItem onClick={() => { handleMenuClose(); navigate('/register'); }}>Register</MenuItem>
        </>
      )}
    </Menu>
  );

  return (
    <AppBar position="static">
      <Toolbar>
        <Typography variant="h6" component={Link} to="/" sx={{ flexGrow: 1, textDecoration: 'none', color: 'white' }}>
          Financial Planner
        </Typography>

        {/* Desktop Menu */}
        <Box sx={{ display: { xs: 'none', md: 'flex' } }}>
          {isAuthenticated ? (
            <>
              {role === 'USER' && (
                <>
                  <Button color="inherit" component={Link} to="/dashboard">Dashboard</Button>
                  <Button color="inherit" component={Link} to="/create-plan">Create Plan</Button>
                  <Button color="inherit" component={Link} to="/timeline">Timeline</Button>
                  <Button color="inherit" component={Link} to="/advisors">Find Advisors</Button>
                </>
              )}
              {role === 'ADVISOR' && (
                <Button color="inherit" component={Link} to="/advisor-dashboard">Dashboard</Button>
              )}
              <IconButton
                edge="end"
                aria-label="account of current user"
                aria-haspopup="true"
                onClick={handleProfileMenuOpen}
                color="inherit"
              >
                <AccountCircle />
              </IconButton>
            </>
          ) : (
            <>
              <Button color="inherit" component={Link} to="/login">Login</Button>
              <Button color="inherit" component={Link} to="/register">Register</Button>
            </>
          )}
        </Box>

        {/* Mobile Menu */}
        <Box sx={{ display: { xs: 'flex', md: 'none' } }}>
          <IconButton
            edge="end"
            aria-label="menu"
            aria-haspopup="true"
            onClick={handleMobileMenuOpen}
            color="inherit"
          >
            <MenuIcon />
          </IconButton>
        </Box>
      </Toolbar>
      {renderMenu}
      {renderMobileMenu}
    </AppBar>
  );
};

export default Navbar;