import { FormEvent, MouseEvent, useState } from "react";
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  AppBar,
  Autocomplete,
  Avatar,
  Badge,
  Box,
  Breadcrumbs,
  Button,
  Card,
  CardActions,
  CardContent,
  Checkbox,
  Chip,
  CircularProgress,
  Container,
  CssBaseline,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Divider,
  Drawer,
  Fab,
  FormControl,
  FormControlLabel,
  FormGroup,
  FormLabel,
  IconButton,
  InputLabel,
  LinearProgress,
  Link,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Menu,
  MenuItem,
  Pagination,
  Paper,
  Radio,
  RadioGroup,
  Rating,
  Select,
  Skeleton,
  Slider,
  Snackbar,
  Stack,
  Switch,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  TextField,
  ThemeProvider,
  Toolbar,
  Tooltip,
  Typography,
  createTheme,
} from "@mui/material";
import {
  Add,
  Analytics,
  CheckCircle,
  Close,
  CloudDone,
  Code,
  DarkMode,
  ExpandMore,
  HelpOutlined,
  LightMode,
  Menu as MenuIcon,
  MoreVert,
  Notifications,
  People,
  RocketLaunch,
  Save,
  Search,
  Settings,
} from "@mui/icons-material";

const skills = ["Kotlin", "Playwright", "React", "Accessibility", "API testing"];

const people = [
  { initials: "AL", name: "Ada Lovelace", role: "Automation architect", status: "Active" },
  { initials: "GH", name: "Grace Hopper", role: "Platform engineer", status: "Review" },
  { initials: "DT", name: "Dorothy Vaughan", role: "Quality lead", status: "Active" },
];

function App() {
  const [darkMode, setDarkMode] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);
  const [tab, setTab] = useState(0);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [snackbarOpen, setSnackbarOpen] = useState(false);
  const [savedName, setSavedName] = useState("");
  const [menuAnchor, setMenuAnchor] = useState<HTMLElement | null>(null);
  const [department, setDepartment] = useState("engineering");

  const theme = createTheme({
    palette: {
      mode: darkMode ? "dark" : "light",
      primary: { main: "#1769aa" },
      secondary: { main: "#7c4dff" },
      background: darkMode
        ? { default: "#0b1622", paper: "#122334" }
        : { default: "#f2f6fa", paper: "#ffffff" },
    },
    shape: { borderRadius: 14 },
    typography: {
      fontFamily: "Inter, ui-sans-serif, system-ui, sans-serif",
      h1: { fontWeight: 800, letterSpacing: "-0.04em" },
      h2: { fontWeight: 750, letterSpacing: "-0.025em" },
      button: { fontWeight: 700, textTransform: "none" },
    },
    components: {
      MuiPaper: {
        styleOverrides: {
          root: { backgroundImage: "none" },
        },
      },
    },
  });

  const handleSubmit = (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault();
    const data = new FormData(event.currentTarget);
    setSavedName(String(data.get("fullName")));
    setSnackbarOpen(true);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <AppBar position="sticky" elevation={0} color="inherit" sx={{ borderBottom: 1, borderColor: "divider" }}>
        <Toolbar>
          <IconButton aria-label="Open navigation" onClick={() => setDrawerOpen(true)} edge="start">
            <MenuIcon />
          </IconButton>
          <RocketLaunch color="primary" sx={{ ml: 1, mr: 1.5 }} />
          <Typography variant="h6" component="div" sx={{ flexGrow: 1, fontWeight: 800 }}>
            Keb UI Laboratory
          </Typography>
          <Tooltip title="Search documentation">
            <IconButton aria-label="Search">
              <Search />
            </IconButton>
          </Tooltip>
          <Tooltip title="Toggle color mode">
            <IconButton
              aria-label="Toggle color mode"
              onClick={() => setDarkMode((value) => !value)}
            >
              {darkMode ? <LightMode /> : <DarkMode />}
            </IconButton>
          </Tooltip>
          <IconButton aria-label="Notifications">
            <Badge badgeContent={3} color="error">
              <Notifications />
            </Badge>
          </IconButton>
          <Avatar sx={{ ml: 1, width: 34, height: 34, bgcolor: "secondary.main" }}>K</Avatar>
        </Toolbar>
      </AppBar>

      <Drawer open={drawerOpen} onClose={() => setDrawerOpen(false)}>
        <Box sx={{ width: 280 }} role="navigation" aria-label="Main navigation">
          <Stack direction="row" sx={{ p: 2, alignItems: "center", justifyContent: "space-between" }}>
            <Typography variant="h6">Explore</Typography>
            <IconButton aria-label="Close navigation" onClick={() => setDrawerOpen(false)}>
              <Close />
            </IconButton>
          </Stack>
          <Divider />
          <List>
            {[
              ["Dashboard", <Analytics />],
              ["Team", <People />],
              ["Components", <Code />],
              ["Settings", <Settings />],
            ].map(([label, icon]) => (
              <ListItem key={label as string}>
                <ListItemAvatar><Avatar>{icon}</Avatar></ListItemAvatar>
                <ListItemText primary={label as string} secondary="Example destination" />
              </ListItem>
            ))}
          </List>
        </Box>
      </Drawer>

      <Container maxWidth="xl" sx={{ py: { xs: 3, md: 5 } }}>
        <Breadcrumbs aria-label="Breadcrumb">
          <Link underline="hover" color="inherit" href="#home">Home</Link>
          <Link underline="hover" color="inherit" href="#laboratory">Examples</Link>
          <Typography color="text.primary">UI laboratory</Typography>
        </Breadcrumbs>

        <Paper
          id="home"
          sx={{
            mt: 3,
            p: { xs: 3, md: 5 },
            overflow: "hidden",
            color: "common.white",
            background: "linear-gradient(125deg, #102a43 0%, #1769aa 58%, #7c4dff 140%)",
          }}
        >
          <Chip label="Keb example workspace" color="secondary" sx={{ mb: 2 }} />
          <Typography variant="h1" sx={{ fontSize: { xs: "2.5rem", md: "4.4rem" }, maxWidth: 850 }}>
            One interface. Many testable interactions.
          </Typography>
          <Typography sx={{ mt: 2, maxWidth: 700, color: "rgba(255,255,255,.78)", fontSize: "1.1rem" }}>
            A compact React and Material UI application designed to exercise browser automation
            against accessible, user-facing controls.
          </Typography>
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mt: 4 }}>
            <Button variant="contained" color="secondary" startIcon={<RocketLaunch />} href="#profile-form">
              Try the form
            </Button>
            <Button variant="outlined" color="inherit" startIcon={<Code />} href="#components">
              Browse components
            </Button>
          </Stack>
        </Paper>

        <Box
          sx={{
            display: "grid",
            gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", lg: "repeat(4, 1fr)" },
            gap: 2,
            my: 3,
          }}
        >
          {[
            ["Test journeys", "24", <CheckCircle color="success" />],
            ["UI controls", "30+", <Code color="primary" />],
            ["Pass rate", "98.7%", <Analytics color="secondary" />],
            ["Browser status", "Ready", <CloudDone color="success" />],
          ].map(([label, value, icon]) => (
            <Card key={label as string} variant="outlined">
              <CardContent>
                <Stack direction="row" sx={{ justifyContent: "space-between" }}>
                  <Box>
                    <Typography color="text.secondary" variant="body2">{label as string}</Typography>
                    <Typography variant="h4" sx={{ mt: 1, fontWeight: 800 }}>{value as string}</Typography>
                  </Box>
                  {icon}
                </Stack>
              </CardContent>
            </Card>
          ))}
        </Box>

        <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", lg: "1.25fr .75fr" }, gap: 3 }}>
          <Paper id="profile-form" component="section" sx={{ p: { xs: 2, md: 3 } }}>
            <Typography variant="h2" sx={{ fontSize: "1.7rem" }}>Profile setup</Typography>
            <Typography color="text.secondary" sx={{ mb: 3 }}>
              Text, choices, date, autocomplete, toggles, and range controls.
            </Typography>

            <Box component="form" onSubmit={handleSubmit}>
              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" }, gap: 2 }}>
                <TextField required name="fullName" label="Full name" defaultValue="Ada Tester" />
                <TextField required name="email" type="email" label="Email address" placeholder="name@example.test" />
                <TextField name="password" type="password" label="Password" />
                <FormControl>
                  <InputLabel id="department-label">Department</InputLabel>
                  <Select
                    name="department"
                    labelId="department-label"
                    label="Department"
                    value={department}
                    onChange={(event) => setDepartment(event.target.value)}
                  >
                    <MenuItem value="engineering">Engineering</MenuItem>
                    <MenuItem value="quality">Quality engineering</MenuItem>
                    <MenuItem value="product">Product</MenuItem>
                  </Select>
                </FormControl>
                <Autocomplete
                  multiple
                  options={skills}
                  defaultValue={["Kotlin"]}
                  renderInput={(params) => <TextField {...params} label="Skills" />}
                />
                <TextField
                  name="startDate"
                  type="date"
                  label="Start date"
                  defaultValue="2026-07-25"
                  slotProps={{ inputLabel: { shrink: true } }}
                />
              </Box>

              <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", md: "1fr 1fr" }, gap: 3, mt: 3 }}>
                <FormControl>
                  <FormLabel>Experience level</FormLabel>
                  <RadioGroup row name="experience" defaultValue="senior">
                    <FormControlLabel value="junior" control={<Radio />} label="Junior" />
                    <FormControlLabel value="senior" control={<Radio />} label="Senior" />
                    <FormControlLabel value="lead" control={<Radio />} label="Lead" />
                  </RadioGroup>
                </FormControl>
                <Box>
                  <Typography id="budget-label" gutterBottom>Automation budget</Typography>
                  <Slider
                    aria-labelledby="budget-label"
                    aria-label="Automation budget"
                    defaultValue={60}
                    valueLabelDisplay="auto"
                    marks={[
                      { value: 0, label: "0" },
                      { value: 100, label: "100" },
                    ]}
                  />
                </Box>
              </Box>

              <FormGroup row sx={{ mt: 2 }}>
                <FormControlLabel name="terms" control={<Checkbox />} label="Accept terms" />
                <FormControlLabel control={<Switch defaultChecked />} label="Email notifications" />
              </FormGroup>

              <Stack direction="row" spacing={2} sx={{ mt: 3 }}>
                <Button type="submit" variant="contained" startIcon={<Save />}>Save profile</Button>
                <Button type="reset" variant="outlined">Reset</Button>
              </Stack>
            </Box>

            {savedName && (
              <Alert severity="success" data-testid="save-summary" sx={{ mt: 3 }}>
                Profile saved for {savedName}
              </Alert>
            )}
          </Paper>

          <Stack spacing={3}>
            <Card variant="outlined">
              <CardContent>
                <Typography variant="h6">Release readiness</Typography>
                <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
                  Component and journey coverage
                </Typography>
                <LinearProgress variant="determinate" value={82} aria-label="Release readiness" />
                <Stack direction="row" spacing={1} sx={{ mt: 2, flexWrap: "wrap", gap: 1 }}>
                  <Chip label="Chromium" color="success" />
                  <Chip label="Firefox" variant="outlined" />
                  <Chip label="WebKit" variant="outlined" />
                </Stack>
              </CardContent>
              <CardActions>
                <Button size="small">View report</Button>
              </CardActions>
            </Card>

            <Paper sx={{ p: 3 }}>
              <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center" }}>
                <Box>
                  <Typography variant="h6">Queue health</Typography>
                  <Typography color="text.secondary">4 jobs currently running</Typography>
                </Box>
                <CircularProgress value={74} variant="determinate" aria-label="Queue health" />
              </Stack>
              <Divider sx={{ my: 2 }} />
              <Skeleton variant="text" width="75%" />
              <Skeleton variant="rounded" height={54} />
            </Paper>

            <Alert severity="info" action={<Button color="inherit" size="small">Details</Button>}>
              Traces are retained for failed journeys.
            </Alert>
          </Stack>
        </Box>

        <Paper id="components" component="section" sx={{ mt: 3, p: { xs: 2, md: 3 } }}>
          <Stack direction="row" sx={{ justifyContent: "space-between", alignItems: "center" }}>
            <Box>
              <Typography variant="h2" sx={{ fontSize: "1.7rem" }}>Component playground</Typography>
              <Typography color="text.secondary">Tabs, accordions, dialogs, menus, and feedback.</Typography>
            </Box>
            <IconButton
              aria-label="More actions"
              onClick={(event: MouseEvent<HTMLElement>) => setMenuAnchor(event.currentTarget)}
            >
              <MoreVert />
            </IconButton>
          </Stack>
          <Menu anchorEl={menuAnchor} open={Boolean(menuAnchor)} onClose={() => setMenuAnchor(null)}>
            <MenuItem onClick={() => setMenuAnchor(null)}>Export report</MenuItem>
            <MenuItem onClick={() => setMenuAnchor(null)}>Duplicate workspace</MenuItem>
          </Menu>

          <Tabs value={tab} onChange={(_, value) => setTab(value)} aria-label="Workspace sections" sx={{ mt: 2 }}>
            <Tab label="Overview" />
            <Tab label="Activity" />
            <Tab label="Settings" />
          </Tabs>
          <Box role="tabpanel" aria-label={["Overview panel", "Activity panel", "Settings panel"][tab]} sx={{ py: 3 }}>
            <Typography data-testid="tab-content">
              {[
                "Overview combines the most important quality signals.",
                "Activity shows the latest browser journeys.",
                "Settings control execution and artifact policies.",
              ][tab]}
            </Typography>
          </Box>

          <Accordion>
            <AccordionSummary expandIcon={<ExpandMore />}>
              <Typography>How does Keb wait?</Typography>
            </AccordionSummary>
            <AccordionDetails>
              <Typography data-testid="waiting-answer">
                Keb delegates actionability and retrying assertions to Playwright.
              </Typography>
            </AccordionDetails>
          </Accordion>
          <Accordion>
            <AccordionSummary expandIcon={<ExpandMore />}>
              <Typography>Where are artifacts stored?</Typography>
            </AccordionSummary>
            <AccordionDetails>
              Screenshots are stored in the configured Keb artifacts directory.
            </AccordionDetails>
          </Accordion>

          <Stack direction={{ xs: "column", sm: "row" }} spacing={2} sx={{ mt: 3 }}>
            <Button variant="contained" onClick={() => setDialogOpen(true)}>Open confirmation dialog</Button>
            <Button variant="outlined" onClick={() => setSnackbarOpen(true)}>Show notification</Button>
            <Tooltip title="Contextual help for this workspace">
              <IconButton aria-label="Workspace help"><HelpOutlined /></IconButton>
            </Tooltip>
          </Stack>
        </Paper>

        <TableContainer component={Paper} sx={{ mt: 3 }}>
          <Table aria-label="Team members">
            <TableHead>
              <TableRow>
                <TableCell>Member</TableCell>
                <TableCell>Role</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Rating</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {people.map((person, index) => (
                <TableRow key={person.name}>
                  <TableCell>
                    <Stack direction="row" spacing={1.5} sx={{ alignItems: "center" }}>
                      <Avatar>{person.initials}</Avatar>
                      <Typography>{person.name}</Typography>
                    </Stack>
                  </TableCell>
                  <TableCell>{person.role}</TableCell>
                  <TableCell><Chip size="small" label={person.status} color={person.status === "Active" ? "success" : "warning"} /></TableCell>
                  <TableCell align="right"><Rating value={5 - index * 0.5} precision={0.5} readOnly /></TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
          <Stack sx={{ p: 2, alignItems: "center" }}>
            <Pagination count={4} color="primary" aria-label="Team pages" />
          </Stack>
        </TableContainer>
      </Container>

      <Tooltip title="Create a new journey">
        <Fab color="secondary" aria-label="Create journey" sx={{ position: "fixed", right: 24, bottom: 24 }}>
          <Add />
        </Fab>
      </Tooltip>

      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)}>
        <DialogTitle>Run the complete suite?</DialogTitle>
        <DialogContent>
          <DialogContentText>
            This demonstrates modal focus management and accessible dialog locators.
          </DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Cancel</Button>
          <Button variant="contained" onClick={() => setDialogOpen(false)}>Run suite</Button>
        </DialogActions>
      </Dialog>

      <Snackbar
        open={snackbarOpen}
        autoHideDuration={4000}
        onClose={() => setSnackbarOpen(false)}
        message="Workspace notification sent"
        action={
          <IconButton color="inherit" aria-label="Close notification" onClick={() => setSnackbarOpen(false)}>
            <Close />
          </IconButton>
        }
      />
    </ThemeProvider>
  );
}

export default App;
