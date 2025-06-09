// src/pages/FinancialPlanDetail.tsx
import { useEffect, useState } from 'react';
import { useParams, Link } from 'react-router-dom';
import { toast } from 'react-toastify';
import { format } from 'date-fns';
import financialPlanService from '../api/financial-plan.service';
import { FinancialPlan } from '@/types';
import {
  Container,
  Typography,
  Grid,
  Paper,
  Box,
  Button,
  Divider,
  Chip,
  CircularProgress,
  Card,
  CardContent,
  List,
  ListItem,
  ListItemText
} from '@mui/material';
import {
  PieChart,
  Pie,
  Cell,
  ResponsiveContainer,
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from 'recharts';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042', '#8884D8'];

const FinancialPlanDetail = () => {
  const { id } = useParams<{ id: string }>();
  const [plan, setPlan] = useState<FinancialPlan | null>(null);
  const [loading, setLoading] = useState(true);

  // Parse JSON data
  const [assetAllocation, setAssetAllocation] = useState<any[]>([]);
  const [goalTimeline, setGoalTimeline] = useState<any[]>([]);
  const [investmentRecommendations, setInvestmentRecommendations] = useState<any[]>([]);
  const [actionPlan, setActionPlan] = useState<any[]>([]);

  useEffect(() => {
    const fetchPlan = async () => {
      try {
        if (!id) return;
        const data = await financialPlanService.getPlanById(parseInt(id));
        console.log(data);
        setPlan(data);

        // Parse JSON data
        if (data.assetAllocationJson) {
          const assetData = JSON.parse(data.assetAllocationJson);
          setAssetAllocation(
            Object.entries(assetData).map(([name, value]) => ({
              name,
              value: Number(value)
            }))
          );
        }

        if (data.goalTimelineJson) {
          setGoalTimeline(JSON.parse(data.goalTimelineJson));
        }

        if (data.investmentRecommendationsJson) {
          setInvestmentRecommendations(JSON.parse(data.investmentRecommendationsJson));
        }

        if (data.actionPlanJson) {
          setActionPlan(JSON.parse(data.actionPlanJson));
        }
      } catch (error) {
        toast.error('Failed to load financial plan');
        console.error('Plan loading error:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchPlan();
  }, [id]);

  const handleDownloadPdf = async () => {
    try {
      if (!id) return;
      const pdfBlob = await financialPlanService.downloadPlanPdf(parseInt(id));

      // Create a link to download the PDF
      const url = window.URL.createObjectURL(pdfBlob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `financial-plan-${id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
    } catch (error) {
      toast.error('Failed to download PDF');
      console.error('PDF download error:', error);
    }
  };

  const handleSharePlan = () => {
    if (!plan) return;

    // Copy share link to clipboard
    const shareLink = `${window.location.origin}/plan/shared/${plan.shareCode}`;
    navigator.clipboard.writeText(shareLink);
    toast.success('Share link copied to clipboard');
  };

  if (loading) {
    return (
      <Container sx={{ mt: 4, display: 'flex', justifyContent: 'center' }}>
        <CircularProgress />
      </Container>
    );
  }

  if (!plan) {
    return (
      <Container sx={{ mt: 4 }}>
        <Typography variant="h5">Financial plan not found</Typography>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ mt: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          {plan.planName}
        </Typography>
        <Box>
          <Button
            variant="outlined"
            onClick={handleSharePlan}
            sx={{ mr: 2 }}
          >
            Share Plan
          </Button>
          <Button
            variant="outlined"
            onClick={handleDownloadPdf}
          >
            Download PDF
          </Button>
        </Box>
      </Box>

      <Typography variant="subtitle1" color="text.secondary" gutterBottom>
        Created on {format(new Date(plan.creationDate), 'MMMM d, yyyy')}
      </Typography>

      <Grid container spacing={4}>
        <Grid size={{xs:12,md:4}}>
          <Paper sx={{ p: 3, mb: 4 }}>
            <Typography variant="h5" gutterBottom>
              Financial Health Score
            </Typography>
            <Box display="flex" alignItems="center" justifyContent="center" my={3}>
              <Box
                sx={{
                  position: 'relative',
                  display: 'inline-flex',
                  width: 200,
                  height: 200
                }}
              >
                <CircularProgress
                  variant="determinate"
                  value={plan.healthScore}
                  size={200}
                  thickness={5}
                  sx={{ color: getHealthScoreColor(plan.healthScore) }}
                />
                <Box
                  sx={{
                    top: 0,
                    left: 0,
                    bottom: 0,
                    right: 0,
                    position: 'absolute',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                  }}
                >
                  <Typography variant="h4" component="div" color="text.secondary">
                    {plan.healthScore}/100
                  </Typography>
                </Box>
              </Box>
            </Box>
            <Divider sx={{ my: 2 }} />
            <Typography variant="body1" paragraph>
              {plan.healthAssessment}
            </Typography>
            <Button
              component={Link}
              to="/advisors"
              variant="contained"
              fullWidth
            >
              Connect with an Advisor
            </Button>
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              Action Plan
            </Typography>
            <List>
              {actionPlan.map((action, index) => (
                <ListItem key={index} alignItems="flex-start" divider={index < actionPlan.length - 1}>
                  <ListItemText
                    primary={action.action}
                    secondary={
                      <>
                        <Typography component="span" variant="body2" color="text.primary">
                          Timeframe: {action.timeframe}
                        </Typography>
                        <br />
                        {action.details}
                      </>
                    }
                  />
                </ListItem>
              ))}
            </List>
          </Paper>
        </Grid>

        <Grid size={{xs:12,md:8}}>
          <Paper sx={{ p: 3, mb: 4 }}>
            <Typography variant="h5" gutterBottom>
              Asset Allocation
            </Typography>
            {assetAllocation.length > 0 ? (
              <Box height={300}>
                <ResponsiveContainer width="100%" height="100%">
                  <PieChart>
                    <Pie
                      data={assetAllocation}
                      cx="50%"
                      cy="50%"
                      labelLine={true}
                      label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                      outerRadius={100}
                      fill="#8884d8"
                      dataKey="value"
                    >
                      {assetAllocation.map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value) => `${value}%`} />
                    <Legend />
                  </PieChart>
                </ResponsiveContainer>
              </Box>
            ) : (
              <Typography variant="body1">
                  No asset allocation data available.
              </Typography>
                )}
              </Paper>

              <Paper sx={{ p: 3, mb: 4 }}>
            <Typography variant="h5" gutterBottom>
              Goal Timeline
            </Typography>
            {goalTimeline.length > 0 ? (
              <Grid container spacing={2}>
                {goalTimeline.map((goal, index) => (
                  <Grid size={{xs:12,sm:6}} key={index}>
                    <Card variant="outlined">
                      <CardContent>
                        <Typography variant="h6" gutterBottom>
                          {goal.goal}
                        </Typography>
                        <Box display="flex" justifyContent="space-between" mb={1}>
                          <Typography variant="body2" color="text.secondary">
                            Timeframe:
                          </Typography>
                          <Typography variant="body1">
                            {goal.timeframe}
                          </Typography>
                        </Box>
                        <Box display="flex" justifyContent="space-between" mb={1}>
                          <Typography variant="body2" color="text.secondary">
                            Target Amount:
                          </Typography>
                          <Typography variant="body1">
                            ${goal.targetAmount.toLocaleString()}
                          </Typography>
                        </Box>
                        <Chip
                          label={goal.priority}
                          color={
                            goal.priority === 'High'
                              ? 'error'
                              : goal.priority === 'Medium'
                                ? 'warning'
                                : 'success'
                          }
                          size="small"
                        />
                      </CardContent>
                    </Card>
                  </Grid>
                ))}
              </Grid>
            ) : (
              <Typography variant="body1">
                No goal timeline data available.
              </Typography>
            )}
          </Paper>

          <Paper sx={{ p: 3 }}>
            <Typography variant="h5" gutterBottom>
              Investment Recommendations
            </Typography>
            {investmentRecommendations.length > 0 ? (
              <>
                <Box mb={3}>
                  <ResponsiveContainer width="100%" height={300}>
                    <PieChart>
                      <Pie
                        data={investmentRecommendations}
                        cx="50%"
                        cy="50%"
                        labelLine={false}
                        label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                        outerRadius={100}
                        fill="#8884d8"
                        dataKey="allocation"
                        nameKey="type"
                      >
                        {investmentRecommendations.map((entry, index) => (
                          <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                        ))}
                      </Pie>
                      <Tooltip formatter={(value) => `${value}%`} />
                    </PieChart>
                  </ResponsiveContainer>
                </Box>

                <Divider sx={{ mb: 2 }} />

                <List>
                  {investmentRecommendations.map((recommendation, index) => (
                    <ListItem key={index} divider={index < investmentRecommendations.length - 1}>
                      <ListItemText
                        primary={
                          <Box display="flex" justifyContent="space-between">
                            <Typography variant="subtitle1">
                              {recommendation.type}
                            </Typography>
                            <Typography variant="subtitle1" color="primary">
                              {recommendation.allocation}%
                            </Typography>
                          </Box>
                        }
                        secondary={recommendation.reasoning}
                      />
                    </ListItem>
                  ))}
                </List>
              </>
            ) : (
              <Typography variant="body1">
                No investment recommendations available.
              </Typography>
            )}
          </Paper>
        </Grid>
      </Grid>
    </Container>
);
};

// Helper function to get color based on health score
const getHealthScoreColor = (score: number) => {
  if (score >= 80) return '#4caf50'; // Green
  if (score >= 60) return '#ff9800'; // Orange
  return '#f44336'; // Red
};

export default FinancialPlanDetail;