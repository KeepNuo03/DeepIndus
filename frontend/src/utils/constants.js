/**
 * 系统常量定义
 */

/**
 * 缺陷类型映射
 * value: 传给后端的英文值（YOLO 类别）
 * label: 前端显示的中文
 */
export const DEFECT_TYPES = [
  { value: 'crazing', label: '龟裂' },
  { value: 'inclusion', label: '夹杂' },
  { value: 'patches', label: '斑块' },
  { value: 'pitted_surface', label: '麻点' },
  { value: 'rolled-in_scale', label: '氧化皮卷入' },
  { value: 'scratches', label: '划伤' },
];

/**
 * 根据英文值获取中文标签
 */
export function getDefectLabel(value) {
  const defect = DEFECT_TYPES.find(d => d.value === value);
  return defect ? defect.label : value;
}

/**
 * 根据中文标签获取英文值
 */
export function getDefectValue(label) {
  const defect = DEFECT_TYPES.find(d => d.label === label);
  return defect ? defect.value : label;
}

/**
 * 缺陷严重程度
 */
export const SEVERITY_LEVELS = {
  critical: '严重',
  warning: '中等',
  minor: '轻微'
};

/**
 * 检测状态
 */
export const DETECTION_STATUS = {
  pass: '合格',
  fail: '不合格'
};
