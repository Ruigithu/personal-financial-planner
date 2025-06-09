import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { toast } from 'react-toastify';
import appointmentService from '../api/appointment.service';
import { AppointmentDetails } from '@/types';
import {
  Container,
  Typography,
  Paper,
  Box,
  Button,
  TextField,
  CircularProgress,
  Grid,
  Divider,
  Alert,
  IconButton,
  Chip
} from '@mui/material';
import {
  VideoCall,
  OpenInNew,
  ContentCopy,
  Schedule,
  Person,
  CheckCircle,
  Error
} from '@mui/icons-material';
import { format } from 'date-fns';

const Meeting = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [appointment, setAppointment] = useState<AppointmentDetails | null>(null);
  const [loading, setLoading] = useState(true);
  const [notes, setNotes] = useState('');
  const [savingNotes, setSavingNotes] = useState(false);
  const [copied, setCopied] = useState(false);

  useEffect(() => {
    const fetchAppointment = async () => {
      try {
        if (!id) return;
        const data = await appointmentService.getAppointmentById(parseInt(id));
        setAppointment(data);

        // If there are advisor notes, pre-fill the form
        if (data.advisorNotes) {
          setNotes(data.advisorNotes);
        }
      } catch (error) {
        toast.error('Failed to load appointment details');
        console.error('Appointment loading error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchAppointment();
  }, [id]);

  // 刷新预约数据
  const refreshAppointmentData = async () => {
    if (!id) return;

    try {
      const data = await appointmentService.getAppointmentById(parseInt(id));
      setAppointment(data);
    } catch (error) {
      console.error('Failed to refresh appointment data:', error);
    }
  };

  // 加入会议
  const handleJoinMeeting = () => {
    if (appointment?.meetingLink) {
      window.open(appointment.meetingLink, '_blank', 'noopener,noreferrer');
    }
  };

  // 复制会议链接
  const handleCopyMeetingLink = async () => {
    if (!appointment?.meetingLink) return;

    try {
      await navigator.clipboard.writeText(appointment.meetingLink);
      setCopied(true);
      toast.success('Meeting link copied to clipboard!');
      setTimeout(() => setCopied(false), 2000);
    } catch (err) {
      // 降级方案
      const textArea = document.createElement('textarea');
      textArea.value = appointment.meetingLink;
      document.body.appendChild(textArea);
      textArea.select();
      document.execCommand('copy');
      document.body.removeChild(textArea);
      setCopied(true);
      toast.success('Meeting link copied to clipboard!');
      setTimeout(() => setCopied(false), 2000);
    }
  };

  const handleSaveNotes = async () => {
    if (!id || !appointment) return;

    setSavingNotes(true);
    try {
      await appointmentService.addAdvisorNotes(parseInt(id), notes);
      toast.success('Notes saved successfully');

      // Update the appointment in the state
      setAppointment({
        ...appointment,
        advisorNotes: notes
      });
    } catch (error) {
      toast.error('Failed to save notes');
      console.error('Save notes error:', error);
    } finally {
      setSavingNotes(false);
    }
  };

  const handleCompleteAppointment = async () => {
    if (!id || !appointment) return;

    try {
      const updatedAppointment = await appointmentService.updateAppointmentStatus(
        parseInt(id),
        'COMPLETED'
      );
      setAppointment(updatedAppointment);
      toast.success('Appointment marked as completed');
    } catch (error) {
      toast.error('Failed to update appointment status');
      console.error('Status update error:', error);
    }
  };

  // 获取会议状态
  const getMeetingStatus = () => {
    if (!appointment) return { status: 'unknown', color: 'default' };

    if (appointment.meetingLink) {
      return { status: 'ready', color: 'success' };
    } else if (appointment.status === 'CONFIRMED') {
      return { status: 'pending', color: 'warning' };
    } else {
      return { status: 'not_available', color: 'error' };
    }
  };

  const isUpcoming = () => {
    if (!appointment) return false;
    return new Date(appointment.appointmentDate) > new Date();
  };

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (!appointment) {
    return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5">Appointment not found</Typography>
      </Container>
    );
  }

  if (appointment.status !== 'CONFIRMED' && appointment.status !== 'COMPLETED') {
    return (
      <Container sx={{ mt: 4 }}>
        <Paper sx={{ p: 3 }}>
          <Typography variant="h5" gutterBottom>
            This appointment is not active
          </Typography>
          <Typography variant="body1" paragraph>
            The appointment status is: {appointment.status}
          </Typography>
          <Button
            variant="contained"
            onClick={() => navigate('/dashboard')}
          >
            Back to Dashboard
          </Button>
        </Paper>
      </Container>
    );
  }

  const meetingStatus = getMeetingStatus();

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Virtual Meeting
      </Typography>

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 8 }}>
          {/* 会议区域 */}
          <Paper sx={{ p: 3, mb: 3 }}>
            <Box display="flex" alignItems="center" justifyContent="space-between" mb={2}>
              <Typography variant="h5">
                Video Conference
              </Typography>
              <Chip
                label={meetingStatus.status.replace('_', ' ').toUpperCase()}
                color={meetingStatus.color as any}
                icon={meetingStatus.status === 'ready' ? <CheckCircle /> : <Schedule />}
              />
            </Box>

            {/* 会议链接区域 */}
            {appointment.meetingLink ? (
              <Box>
                <Alert severity="success" sx={{ mb: 2 }}>
                  <Typography variant="body2" sx={{ mb: 1 }}>
                    <strong>Meeting Ready!</strong> Click below to join your Google Meet session.
                  </Typography>
                  <Box display="flex" alignItems="center" gap={1} mt={1}>
                    <TextField
                      size="small"
                      value={appointment.meetingLink}
                      InputProps={{ readOnly: true }}
                      sx={{ flexGrow: 1, fontSize: '0.875rem' }}
                    />
                    <IconButton
                      onClick={handleCopyMeetingLink}
                      color={copied ? 'success' : 'default'}
                      size="small"
                      title="Copy meeting link"
                    >
                      {copied ? <CheckCircle /> : <ContentCopy />}
                    </IconButton>
                  </Box>
                </Alert>

                <Box mb={3}>
                  <Button
                    variant="contained"
                    size="large"
                    startIcon={<VideoCall />}
                    endIcon={<OpenInNew />}
                    onClick={handleJoinMeeting}
                    disabled={!isUpcoming() && appointment.status !== 'CONFIRMED'}
                    fullWidth
                  >
                    Join Meeting
                  </Button>
                </Box>

                {!isUpcoming() && appointment.status !== 'COMPLETED' && (
                  <Alert severity="info" sx={{ mb: 2 }}>
                    Meeting time has passed. You can still join if the session is ongoing.
                  </Alert>
                )}
              </Box>
            ) : (
              <Box>
                <Alert severity="warning" sx={{ mb: 2 }}>
                  <Typography variant="body2">
                    Meeting link is not available for this appointment. Please contact support if you need assistance.
                  </Typography>
                </Alert>
              </Box>
            )}

            {/* 会议说明 */}
            <Box mt={3} p={2} sx={{ backgroundColor: 'grey.50', borderRadius: 1 }}>
              <Typography variant="subtitle2" gutterBottom>
                Meeting Instructions:
              </Typography>
              <Typography variant="body2" component="ul" sx={{ pl: 2, margin: 0 }}>
                <li>Click "Join Meeting" to open Google Meet in a new tab</li>
                <li>Test your camera and microphone before joining</li>
                <li>Join 2-3 minutes early to resolve any technical issues</li>
                <li>Have your questions and documents ready</li>
              </Typography>
            </Box>

            <Box mt={3}>
              <Button
                variant="contained"
                color="error"
                onClick={() => navigate('/dashboard')}
              >
                End Meeting
              </Button>
            </Box>
          </Paper>

          {/* 共享财务计划 */}
          {appointment.sharedPlanId && (
            <Paper sx={{ p: 3 }}>
              <Typography variant="h5" gutterBottom>
                Shared Financial Plan: {appointment.sharedPlanName}
              </Typography>
              <Typography variant="body1" paragraph>
                {appointment.sharedPlanHealthAssessment}
              </Typography>
              <Button
                variant="outlined"
                onClick={() => window.open(`/plan/${appointment.sharedPlanId}`, '_blank')}
              >
                View Full Plan
              </Button>
            </Paper>
          )}
        </Grid>

        <Grid size={{ xs: 12, md: 4 }}>
          {/* 预约详情 */}
          <Paper sx={{ p: 3, mb: 3 }}>
            <Typography variant="h6" gutterBottom display="flex" alignItems="center">
              <Person sx={{ mr: 1 }} />
              Appointment Details
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>With:</strong> {appointment.advisorFirstName} {appointment.advisorLastName}
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>Date & Time:</strong> {format(new Date(appointment.appointmentDate), 'MMMM d, yyyy h:mm a')}
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>Duration:</strong> {appointment.durationMinutes} minutes
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>Session Type:</strong> {appointment.sessionType.replace(/_/g, ' ')}
            </Typography>
            <Typography variant="body1" gutterBottom>
              <strong>Status:</strong> {appointment.status}
            </Typography>

            {appointment.userNotes && (
              <>
                <Divider sx={{ my: 2 }} />
                <Typography variant="subtitle1" gutterBottom>
                  Client Notes:
                </Typography>
                <Typography variant="body1" paragraph>
                  {appointment.userNotes}
                </Typography>
              </>
            )}
          </Paper>

          {/* 顾问笔记 */}
          <Paper sx={{ p: 3 }}>
            <Typography variant="h6" gutterBottom>
              Advisor Notes
            </Typography>
            <TextField
              fullWidth
              multiline
              rows={6}
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
              placeholder="Enter session notes, recommendations, and action items..."
              sx={{ mb: 2 }}
            />
            <Box display="flex" gap={2}>
              <Button
                variant="outlined"
                onClick={handleSaveNotes}
                disabled={savingNotes}
              >
                {savingNotes ? 'Saving...' : 'Save Notes'}
              </Button>

              {appointment.status !== 'COMPLETED' && (
                <Button
                  variant="contained"
                  color="success"
                  onClick={handleCompleteAppointment}
                >
                  Complete Appointment
                </Button>
              )}
            </Box>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default Meeting;