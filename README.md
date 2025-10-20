# Task Manager Frontend A simple React web application for managing tasks with command execution capabilities. ## Features - Create, view, edit, and delete tasks - Execute shell commands in Kubernetes pods - View command execution results - Search and filter tasks - Simple dashboard with task statistics ## Technology Stack - React 19 with TypeScript - Ant Design UI components - Vite for development and building - Axios for API communication ## Prerequisites - Node.js (version 18 or higher) - npm or yarn package manager - Task Manager backend API running on port 8081 ## Installation 1. Install dependencies:
bash
npm install
2. Start the development server:
bash
npm run dev
3. Open your browser and go to http://localhost:3001 ## Usage ### Dashboard - View total tasks, executed tasks, and pending tasks - See a list of all tasks with their current status ### Create Task - Fill in the task form with ID, name, owner, and command - Click "Create Task" to save ### Manage Tasks - View all tasks in a list format - Search tasks by name, owner, or command - Execute tasks to run commands in Kubernetes - Edit existing tasks - Delete tasks when no longer needed ### Task Execution - Click "Execute" on any task to run its command - View the command output directly in the task list - See execution status (Executed/Not Executed) ## API Integration The frontend connects to the backend API at http://localhost:8081 with these endpoints: - GET /tasks - Get all tasks - PUT /tasks - Create or update a task - DELETE /tasks/{id} - Delete a task - PUT /tasks/{id}/execute - Execute a task command - GET /tasks/findByName - Search tasks by name ## Project Structure
src/
├── components/
│   ├── Dashboard.tsx       # Main dashboard page
│   ├── TaskForm.tsx        # Create/edit task form
│   └── TaskList.tsx        # Task listing and management
├── hooks/
│   └── useTasks.ts         # Custom hook for task operations
├── services/
│   └── api.ts              # API service functions
├── types/
│   └── index.ts            # TypeScript type definitions
├── utils/
│   └── index.ts            # Utility functions
├── App.tsx                 # Main application component
└── main.tsx                # Application entry point
## Available Scripts - npm run dev - Start development server - npm run build - Build for production - npm run preview - Preview production build - npm run lint - Run code linting ## Configuration The application uses Vite's proxy configuration to forward API requests from /api/* to http://localhost:8081. ## Troubleshooting **Connection Issues:** - Make sure the backend API is running on port 8081 - Check that MongoDB is running (required by the backend) - Verify there are no firewall issues blocking the connection **Build Issues:** - Delete node_modules and run npm install again - Make sure you have the correct Node.js version installed **Runtime Errors:** - Check the browser console for detailed error messages - Verify all required environment variables are set
