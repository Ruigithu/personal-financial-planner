import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import financialPlanService from '../api/financial-plan.service';
import appointmentService from '../api/appointment.service';
import { FinancialPlan, Appointment, AppointmentDetails } from '@/types';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  Stepper,
  Step,
  StepLabel,
  StepContent,
  Card,
  CardContent,
  Divider,
  Chip,
  CircularProgress
} from '@mui/material';
import FinancialPlanDetails from '@/pages/FinancialPlanDetails.tsx';

type TimelineEvent = {
  id: number;
  type: 'plan' | 'appointment';
  date: string;
  title: string;
  content: string;
  data: FinancialPlan | AppointmentDetails;
};

const Timeline = () => {
  const [events, setEvents] = useState<TimelineEvent[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const [plansData, appointmentsData] = await Promise.all([
          financialPlanService.getUserPlans(),
          appointmentService.getUserAppointments()
        ]);

        // Convert plans to timeline events
        const planEvents = plansData.map(plan => ({
          id: plan.id,
          type: 'plan' as const,
          date: plan.creationDate,
          title: `Created Financial Plan: ${plan.planName}`,
          content: plan.healthAssessment,
          data: plan
        }));

        // Convert completed appointments to timeline events
        const appointmentEvents = appointmentsData
          .filter(app => app.status === 'COMPLETED')
          .map(appointment => ({
            id: appointment.id,
            type: 'appointment' as const,
            date: appointment.appointmentDate,
            title: `Meeting with ${appointment.advisorFirstName} ${appointment.advisorLastName}`,
            content: appointment.advisorNotes || 'No notes from the advisor.',
            data: appointment
          }));

        // Combine and sort by date
        const allEvents = [...planEvents, ...appointmentEvents].sort(
          (a, b) => new Date(a.date).getTime() - new Date(b.date).getTime()
        );

        setEvents(allEvents);
      } catch (error) {
        toast.error('Failed to load timeline data');
        console.error('Timeline error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Your Financial Journey
      </Typography>

      {events.length === 0 ? (
        <Paper sx={{ p: 3 }}>
          <Typography variant="body1" paragraph>
            You haven't created any financial plans or had any meetings yet.
            Start your financial journey today!
          </Typography>
          <Button
            variant="contained"
            component={Link}
            to="/create-plan"
          >
            Create Your First Plan
          </Button>
        </Paper>
      ) : (
        <Stepper orientation="vertical">
          {events.map((event, index) => (
            <Step key={index} active={true}>
              <StepLabel>
                <Box display="flex" justifyContent="space-between" alignItems="center">
                  <Typography variant="subtitle1">
                    {event.title}
                  </Typography>
                  <Typography variant="body2" color="text.secondary">
                    {format(new Date(event.date), 'MMMM d, yyyy')}
                    {event.type === 'appointment' &&
                      ` at ${format(new Date(event.date), 'h:mm a')}`}
                  </Typography>
                </Box>
              </StepLabel>
              <StepContent>
                <Card variant="outlined" sx={{ mb: 2 }}>
                  <CardContent>
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                      <Chip
                        label={event.type === 'plan' ? 'Financial Plan' : 'Advisor Meeting'}
                        color={event.type === 'plan' ? 'primary' : 'secondary'}
                        size="small"
                      />
                    </Box>
                    <Divider sx={{ mb: 2 }} />
                    <Typography variant="body1" paragraph>
                      {event.content}
                    </Typography>
                    <Button
                      variant="outlined"
                      size="small"
                      component={Link}
                      to={event.type === 'plan'
                        ? `/plan/${event.id}`
                        : `/appointment/${event.id}`}
                    >
                      View Details
                    </Button>
                  </CardContent>
                </Card>
              </StepContent>
            </Step>
          ))}
        </Stepper>
      )}
    </Container>
  );
};

export default Timeline;