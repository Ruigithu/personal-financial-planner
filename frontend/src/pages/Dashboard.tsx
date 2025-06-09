import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import financialPlanService from '../api/financial-plan.service';
import appointmentService from '../api/appointment.service';
import { FinancialPlan, AppointmentDetails } from '@/types';
import {
  Container,
  Typography,
  Button,
  Grid,
  Paper,
  Box,
  Card,
  CardContent,
  CardActions,
  CircularProgress,
  Chip,
  Avatar,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  ListItemSecondaryAction,
  IconButton
} from '@mui/material';
import {
  TrendingUp,
  Event,
  Person,
  Add,
  MoreVert,
  Schedule,
  Assessment,
  AccountBalance
} from '@mui/icons-material';
import { format } from 'date-fns';

const Dashboard = () => {
  const [plans, setPlans] = useState<FinancialPlan[]>([]);
  const [appointments, setAppointments] = useState<AppointmentDetails[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [plansData, appointmentsData] = await Promise.all([
          financialPlanService.getUserPlans(),
          appointmentService.getUserAppointments()
        ]);

        setPlans(Array.isArray(plansData) ? plansData : []);
        setAppointments(
          Array.isArray(appointmentsData)
            ? appointmentsData.filter(a => a.status !== 'CANCELLED' && a.status !== 'COMPLETED')
            : []
        );
      } catch (error) {
        toast.error('Failed to load dashboard data');
        setPlans([]);
        setAppointments([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  const getHealthScoreColor = (score: number) => {
    if (score >= 80) return 'success';
    if (score >= 60) return 'warning';
    return 'error';
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFIRMED': return 'success';
      case 'CANCELLED': return 'error';
      case 'COMPLETED': return 'default';
      default: return 'primary';
    }
  };

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      {/* 顶部欢迎区域 */}
      <Box mb={4}>
        <Typography variant="h4" component="h1" gutterBottom fontWeight="bold">
          Welcome back! 👋
        </Typography>
        <Typography variant="h6" color="text.secondary">
          Here's what's happening with your financial journey
        </Typography>
      </Box>


      {/* 主要内容区域 */}
      <Grid container spacing={3}>
        {/* 左侧：财务计划 */}
        <Grid size={{ xs: 12, lg: 8 }}>
          <Paper sx={{ p: 3, height: 'fit-content' }}>
            <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
              <Box display="flex" alignItems="center">
                <Assessment sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h5" fontWeight="600">
                  Your Financial Plans
                </Typography>
              </Box>
              <Button
                variant="contained"
                startIcon={<Add />}
                component={Link}
                to="/create-plan"
                sx={{ borderRadius: 2 }}
              >
                Create New Plan
              </Button>
            </Box>

            {plans.length === 0 ? (
              <Box textAlign="center" py={4}>
                <Assessment sx={{ fontSize: 64, color: 'text.secondary', mb: 2 }} />
                <Typography variant="h6" gutterBottom>
                  No Financial Plans Yet
                </Typography>
                <Typography variant="body1" color="text.secondary" mb={3}>
                  Create your first financial plan to get personalized recommendations and track your progress.
                </Typography>
                <Button
                  variant="contained"
                  size="large"
                  startIcon={<Add />}
                  component={Link}
                  to="/create-plan"
                >
                  Create Your First Plan
                </Button>
              </Box>
            ) : (
              <>
                <Grid container spacing={3}>
                  {plans.slice(0, 6).map((plan) => (
                    <Grid size={{ xs: 12, sm: 6, md: 4 }} key={plan.id}>
                      <Card
                        sx={{
                          height: '100%',
                          transition: 'all 0.3s ease',
                          '&:hover': {
                            transform: 'translateY(-4px)',
                            boxShadow: 4
                          }
                        }}
                      >
                        <CardContent>
                          <Box display="flex" justifyContent="space-between" alignItems="flex-start" mb={2}>
                            <Typography variant="h6" component="div" noWrap sx={{ fontWeight: 600 }}>
                              {plan.planName}
                            </Typography>
                            <IconButton size="small">
                              <MoreVert />
                            </IconButton>
                          </Box>

                          <Typography color="text.secondary" gutterBottom variant="body2">
                            Created {format(new Date(plan.creationDate), 'MMM d, yyyy')}
                          </Typography>

                          <Box display="flex" alignItems="center" justifyContent="space-between" mt={2}>
                            <Typography variant="body2" color="text.secondary">
                              Health Score
                            </Typography>
                            <Chip
                              label={`${plan.healthScore}/100`}
                              color={getHealthScoreColor(plan.healthScore) as any}
                              size="small"
                              sx={{ fontWeight: 600 }}
                            />
                          </Box>
                        </CardContent>

                        <CardActions sx={{ justifyContent: 'space-between', px: 2, pb: 2 }}>
                          <Button
                            size="small"
                            component={Link}
                            to={`/plan/${plan.id}`}
                            sx={{ fontWeight: 600 }}
                          >
                            View Details
                          </Button>
                        </CardActions>
                      </Card>
                    </Grid>
                  ))}
                </Grid>

                {plans.length > 6 && (
                  <Box mt={3} textAlign="center">
                    <Button
                      component={Link}
                      to="/timeline"
                      variant="outlined"
                      size="large"
                    >
                      View All {plans.length} Plans
                    </Button>
                  </Box>
                )}
              </>
            )}
          </Paper>
        </Grid>

        {/* 右侧：预约和快捷操作 */}
        <Grid size={{ xs: 12, lg: 4 }}>
          <Box display="flex" flexDirection="column" gap={3}>
            {/* 即将到来的预约 */}
            <Paper sx={{ p: 3 }}>
              <Box display="flex" alignItems="center" mb={2}>
                <Schedule sx={{ mr: 1, color: 'primary.main' }} />
                <Typography variant="h6" fontWeight="600">
                  Upcoming Appointments
                </Typography>
              </Box>

              {appointments.length === 0 ? (
                <Box textAlign="center" py={3}>
                  <Event sx={{ fontSize: 48, color: 'text.secondary', mb: 1 }} />
                  <Typography variant="body1" color="text.secondary">
                    No upcoming appointments
                  </Typography>
                </Box>
              ) : (
                <List sx={{ p: 0 }}>
                  {appointments.slice(0, 3).map((appointment, index) => (
                    <ListItem
                      key={appointment.id}
                      sx={{
                        px: 0,
                        borderRadius: 1,
                        mb: 1,
                        '&:hover': { bgcolor: 'action.hover' }
                      }}
                    >
                      <ListItemAvatar>
                        <Avatar sx={{ bgcolor: 'primary.main' }}>
                          <Person />
                        </Avatar>
                      </ListItemAvatar>
                      <ListItemText
                        primary={
                          <Typography variant="subtitle2" fontWeight="600">
                            {format(new Date(appointment.appointmentDate), 'MMM d, h:mm a')}
                          </Typography>
                        }
                        secondary={
                          <Box>
                            <Typography variant="body2" color="text.secondary">
                              {appointment.advisorFirstName} {appointment.advisorLastName}
                            </Typography>
                            <Box display="flex" alignItems="center" gap={1} mt={0.5}>
                              <Chip
                                label={appointment.status}
                                color={getStatusColor(appointment.status) as any}
                                size="small"
                                sx={{ height: 20, fontSize: '0.75rem' }}
                              />
                            </Box>
                          </Box>
                        }
                      />
                      <ListItemSecondaryAction>
                        <Button
                          size="small"
                          component={Link}
                          to={`/appointment/${appointment.id}`}
                          variant="outlined"
                        >
                          Details
                        </Button>
                      </ListItemSecondaryAction>
                    </ListItem>
                  ))}
                </List>
              )}

              <Button
                variant="contained"
                fullWidth
                component={Link}
                to="/advisors"
                sx={{ mt: 2, borderRadius: 2 }}
                startIcon={<Person />}
              >
                Find an Advisor
              </Button>
            </Paper>

            {/* 快捷操作 */}
            <Paper sx={{ p: 3 }}>
              <Typography variant="h6" gutterBottom fontWeight="600">
                Quick Actions
              </Typography>

              <Box display="flex" flexDirection="column" gap={2}>
                <Button
                  variant="outlined"
                  fullWidth
                  component={Link}
                  to="/create-plan"
                  startIcon={<Add />}
                  sx={{ justifyContent: 'flex-start', py: 1.5 }}
                >
                  Create Financial Plan
                </Button>

                <Button
                  variant="outlined"
                  fullWidth
                  component={Link}
                  to="/timeline"
                  startIcon={<Schedule />}
                  sx={{ justifyContent: 'flex-start', py: 1.5 }}
                >
                  View Timeline
                </Button>

                <Button
                  variant="outlined"
                  fullWidth
                  component={Link}
                  to="/profile"
                  startIcon={<Person />}
                  sx={{ justifyContent: 'flex-start', py: 1.5 }}
                >
                  Update Profile
                </Button>
              </Box>
            </Paper>
          </Box>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Dashboard;